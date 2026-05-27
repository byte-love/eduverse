package com.eduverse.aigc.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.eduverse.aigc.agent.Agent;
import com.eduverse.aigc.config.ToolResultHolder;
import com.eduverse.aigc.constants.Constant;
import com.eduverse.aigc.enums.ChatEventTypeEnum;
import com.eduverse.aigc.service.ChatService;
import com.eduverse.aigc.service.ChatSessionService;
import com.eduverse.aigc.vo.ChatEventVO;
import com.eduverse.common.utils.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractAgent implements Agent {

    @Resource
    private ChatSessionService chatSessionService;
    @Resource
    private ChatClient agentChatClient;
    @Resource
    private ChatMemory redisChatMemory;

    // 输出结束的标记
    public static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    // 存储大模型的生成状态，这里采用ConcurrentHashMap是确保线程安全
    // 目前的版本暂时用Map实现，如果考虑分布式环境的话，可以考虑用redis来实现
    public static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();

    /**
     * 处理标准请求（非流式）
     * @param question  用户输入的问题
     * @param sessionId 会话唯一标识
     * @return 处理结果
     */
    @Override
    public String process(String question, String sessionId) {
        // 获取用户id
        var userId = UserContext.getUser();
        var requestId = this.generateRequestId();

        //更新会话时间
        this.chatSessionService.update(sessionId, question, userId);

        return this.getChatClientRequest(sessionId, requestId, question)
                .call()
                .content();
    }

    /**
     * 处理流式请求（如流式回答）
     * @param question  用户输入的问题
     * @param sessionId 会话唯一标识
     * @return
     */
    public Flux<ChatEventVO> processStream(String question, String sessionId) {
        // 获取用户id
        var userId = UserContext.getUser();
        var requestId = this.generateRequestId();
        // 大模型输出内容的缓存器，用于在输出中断后的数据存储
        var outputBuilder = new StringBuilder();
        // 获取对话id
        var conversationId = ChatService.getConversationId(sessionId);

        return this.getChatClientRequest(sessionId, requestId, question)
                .stream()
                .chatResponse()
                .doFirst(() -> GENERATE_STATUS.put(sessionId, true)) // 第一次输出内容时执行
                .doOnError(throwable -> GENERATE_STATUS.remove(sessionId)) // 出现异常时，删除标识
                .doOnComplete(() -> GENERATE_STATUS.remove(sessionId)) // 完成时执行，删除标识
                .doOnCancel(() -> {
                    // 当输出被取消时，保存输出的内容到历史记录中
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
                .takeWhile(response -> { // 通过返回值来控制Flux流是否继续，true：继续，false：终止
                    return GENERATE_STATUS.getOrDefault(sessionId, false);
                })
                .map(chatResponse -> {
                    var finishReason = chatResponse.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals(Constant.STOP, finishReason)) {
                        var messageId = chatResponse.getMetadata().getId();
                        ToolResultHolder.put(messageId, Constant.REQUEST_ID, requestId);
                    }

                    // 获取大模型的输出的内容
                    var text = chatResponse.getResult().getOutput().getText();
                    // 追加到输出内容中
                    outputBuilder.append(text);
                    // 封装响应对象
                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .doFinally(signalType -> {
                    // 需要更新对话的标题 或 更新时间
                    var content = StrUtil.format("""
                            ------------
                            USER:{} \n
                            ASSISTANT:{}
                            ------------
                            """, question, outputBuilder.toString());
                    this.chatSessionService.update(sessionId, content, userId);
                })
                .concatWith(Flux.defer(() -> {
                    // 给子类一个后处理机会（如强制调用工具方法填充ToolResultHolder）
                    this.afterStream(requestId, sessionId);
                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
                    var map = ToolResultHolder.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        ToolResultHolder.remove(requestId); // 清除参数列表

                        // 响应给前端的参数数据
                        var chatEventVO = ChatEventVO.builder()
                                .eventData(map)
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .build();
                        return Flux.just(chatEventVO, STOP_EVENT);
                    }
                    return Flux.just(STOP_EVENT);
                }));
    }

    /**
     * 获取大模型请求对象
     * @param sessionId
     * @param requestId
     * @param question
     * @return
     */
    private ChatClient.ChatClientRequestSpec getChatClientRequest(String sessionId, String requestId, String question) {
        return this.agentChatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec.text(this.systemMessage()).params(this.systemMessageParams()))
                .advisors(advisor -> advisor.advisors(this.advisors()).params(this.advisorParams(sessionId, requestId)))
                .tools(this.tools())
                .toolContext(this.toolContext(sessionId, requestId))
                .user(question);
    }

    /**
     * 保存停止输出的记录
     *
     * @param conversationId 对话id
     * @param content   大模型输出的内容
     */
    private void saveStopHistoryRecord(String conversationId, String content) {
        this.redisChatMemory.add(conversationId, new AssistantMessage(content));
    }

    /**
     * 生成请求id
     * @return
     */
    private String generateRequestId() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * Advisor的参数列表
     * @param sessionId
     * @param requestId
     * @return
     */
    @Override
    public Map<String, Object> advisorParams(String sessionId, String requestId) {
        var conversationId = ChatService.getConversationId(sessionId);
        return Map.of(ChatMemory.CONVERSATION_ID, conversationId);
    }

    /**
     * 流式输出完成后的钩子，子类可在此填充ToolResultHolder。
     */
    protected void afterStream(String requestId, String sessionId) {
    }

    /**
     * 停止指定会话的处理
     * @param sessionId 
     */
    @Override
    public void stop(String sessionId) {
        GENERATE_STATUS.remove(sessionId);
    }
}