package com.eduverse.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.eduverse.aigc.config.SystemPromptConfig;
import com.eduverse.aigc.config.ToolResultHolder;
import com.eduverse.aigc.constants.Constant;
import com.eduverse.aigc.enums.ChatEventTypeEnum;
import com.eduverse.aigc.service.ChatService;
import com.eduverse.aigc.service.ChatSessionService;
import com.eduverse.aigc.vo.ChatEventVO;
import com.eduverse.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tj.ai", name = "chat-type", havingValue = "ENHANCE") // 从系统配置文件中获取当前配置文件中的chat-type属性，用来决定选用哪种智能体模式
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;

    private final SystemPromptConfig systemPromptConfig;

    private final ChatMemory redisChatMemory;

    private final VectorStore vectorStore;

    private final ChatSessionService chatSessionService;

    // 定义一个线程安全的map容器，存储sessionId和是否继续接收大模型输出信息的开关
    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();

    // 定义一个结束的标记
    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    /**
     * AI助理聊天对话
     * @param question  用户的问题
     * @param sessionId 会话id
     * @return ChatEventVO
     */
    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        // 对话Id = 用户Id_会话Id
        String conversationId = ChatService.getConversationId(sessionId);
        // 创建一个容器拼接大模型输出的内容（用户手动打断大模型输出后会出现不保存大模型数据的bug）
        StringBuilder outputBuilder = new StringBuilder();
        // 生成一个请求Id
        String requestId = IdUtil.fastSimpleUUID();
        // 获取用户Id
        Long userId = UserContext.getUser();
        // 更新会话信息
        // chatSessionService.update(sessionId, question, userId);
        // RAG增强，相似度阈值0.6，返回前5条数据
        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.6).topK(5).build()).build();
        return chatClient.prompt()
                .system(promptSystemSpec ->
                    promptSystemSpec
                        // 设置系统提示词
                        .text(systemPromptConfig.getChatSystemMessage().get())
                        // 设置当前时间参数（nacos配置文件中的文本文件里的参数）
                        .param("now", DateUtil.now()))
                .user(question)
                .advisors(advisorSpec -> {
                    advisorSpec
                        //设置RAG增强
                        .advisors(qaAdvisor)
                        // 告诉记忆组件：“请去加载 ID 为 conversationId 的历史记录，并把它们作为上下文发给大模型。”
                        .param(ChatMemory.CONVERSATION_ID, conversationId);
                })
                // 通过工具上下文传递requestId
                .toolContext(Map.of(
                        Constant.REQUEST_ID, requestId,
                        Constant.USER_ID, userId)
                )
                .stream()
                .chatResponse()
                // 1.第一次沟通时，设置sessionId为true，表示可以继续接收大模型输出的数据
                .doFirst(() -> {GENERATE_STATUS.put(sessionId, true);})
                .doOnError(throwable -> {
                    // 2.当流出错时，删除sessionId
                    GENERATE_STATUS.remove(sessionId);
                })
                // 3.当流结束后，删除sessionId
                .doOnComplete(() -> {GENERATE_STATUS.remove(sessionId);})
                .doOnCancel(() -> {
                    // 5.当流结束后，将拼接好的大模型输出手动保存到redis中，这个语句必须在takeWhile之前
                    redisChatMemory.add(conversationId, new AssistantMessage(outputBuilder.toString()));
                })
                .takeWhile(response -> {
                    // 4.此方法用来控制flux流是否继续接收数据：true表示继续接收大模型输出的数据，反之拒绝接收
                    return GENERATE_STATUS.getOrDefault(sessionId, true);
                })
                .map(response -> {
                    // 每次接收到数据时，判断是否结束会话，是的话再将消息id，requestId 存入容器中
                    String finishReason = response.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals(Constant.STOP, finishReason)) {
                        // 获取消息id 将requestId 消息id requestId 存入容器中
                        String messageId = response.getMetadata().getId();
                        ToolResultHolder.put(messageId, Constant.REQUEST_ID, requestId);
                    }
                    // 封装成ChatEventVO
                    String text = response.getResult().getOutput().getText();
                    // 拼接大模型输出
                    outputBuilder.append(text);
                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .doFinally(signalType -> {
                    // 将会话的内容（用户第一次问的问题和大模型回答的答案一起发送给chatsessionservice，
                    // 调用update方法让另一个大模型总结生成一个标题）
                    var content = StrUtil.format("""
                        ------------
                        USER:{} \n
                        ASSISTANT:{}
                        ------------
                        """, question, outputBuilder.toString());
                    this.chatSessionService.update(sessionId, content, userId);
                })
                .concatWith(Flux.defer(() -> {
                    // 延迟创建的Flux流，会在所有数据都接收完成后执行
                    Map<String, Object> map = ToolResultHolder.get(requestId);
                    if (MapUtil.isNotEmpty(map)) {
                        // 1. 如果数据容器中有课程卡片信息，需要手动拼接(VO + 结束符)
                        ChatEventVO chatEventVO = ChatEventVO.builder()
                                .eventData(map)
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .build();
                        // 删除容器中的数据（课程卡片）
                        ToolResultHolder.remove(requestId);
                        return Flux.just(chatEventVO, STOP_EVENT);
                    }else {
                        // 2. 如果没有课程卡片信息，则直接返回结束流
                        return Flux.just(STOP_EVENT);
                    }
                }));
    }

    /**
     * 停止会话
     *
     * @param sessionId 会话id
     */
    @Override
    public void stop(String sessionId) {
        GENERATE_STATUS.put(sessionId, false);
    }


}
