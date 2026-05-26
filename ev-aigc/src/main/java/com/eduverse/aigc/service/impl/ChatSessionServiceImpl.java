package com.eduverse.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ServiceException;
import com.eduverse.aigc.config.SessionProperties;
import com.eduverse.aigc.config.SystemPromptConfig;
import com.eduverse.aigc.entity.ChatSession;
import com.eduverse.aigc.enums.MessageTypeEnum;
import com.eduverse.aigc.mapper.ChatSessionMapper;
import com.eduverse.aigc.memory.MyAssistantMessage;
import com.eduverse.aigc.service.ChatService;
import com.eduverse.aigc.service.ChatSessionService;
import com.eduverse.aigc.vo.ChatSessionVO;
import com.eduverse.aigc.vo.MessageVO;
import com.eduverse.aigc.vo.SessionVO;
import com.eduverse.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 29410
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private final SessionProperties sessionProperties;

    private final ChatMemory redisChatMemory;

    private final DashScopeChatModel dashScopeChatModel;

    private final SystemPromptConfig systemPromptConfig;

    /**
     * 创建会话
     * @param num 热门问题的数量
     * @return 会话信息
     */
    @Override
    public SessionVO createSession(Integer num) {
        // 将nacos配置参数copy到会话VO中
        SessionVO sessionVO = BeanUtil.toBean(sessionProperties, SessionVO.class);
        // 随机获取num个问题并设置到会话VO中
        sessionVO.setExamples(RandomUtil.randomEleList(sessionProperties.getExamples(), num));
        // 会话id
        sessionVO.setSessionId(IdUtil.simpleUUID());
        // 创建ChatSession对象，并保存到数据库中
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionVO.getSessionId())
                .userId(UserContext.getUser())
                .build();
        super.save(chatSession);
        // 返回结果
        return sessionVO;
    }

    /**
     * 获取热门问题
     * @param num 获取的个数
     * @return 热门问题
     */
    @Override
    public List<SessionVO.Example> hotExamples(Integer num) {
        return RandomUtil.randomEleList(sessionProperties.getExamples(), num);
    }

    /**
     * 根据会话id查询单个会话详情
     * @param sessionId 会话id
     * @return 消息列表
     */
    @Override
    public List<MessageVO> queryBySessionId(String sessionId) {
        // 获取对话id
        String conversationId = ChatService.getConversationId(sessionId);
        // 从ChatMemory中获取消息列表
        List<Message> messageList = redisChatMemory.get(conversationId);
        // 将消息列表转换为MessageVO列表，只返回用户和助手的会话消息，因为自定义的消息类的类型中只有用户和助手两种类型
        return messageList.stream()
                .filter(message -> message.getMessageType() == MessageType.ASSISTANT || message.getMessageType() == MessageType.USER)
                .map(message -> {
                    // 判断是自定义大模型回答消息
                    if (message instanceof MyAssistantMessage myassistantMessage) {
                        return MessageVO.builder()
                                .content(message.getText())
                                .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                                .params(myassistantMessage.getParams())
                                .build();
                    } else {
                        // 否则，返回普通消息
                        return MessageVO.builder()
                                .content(message.getText())
                                .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                                .build();
                    }

                }
        ).toList();
    }

    /**
     * 更新会话
     *
     * @param sessionId 会话ID，用于标识特定的聊天会话
     * @param title     新的会话标题，如果为空则不进行更新
     * @param userId    用户ID
     */
    // 异步方法
    @Async
    @Override
    public void update(String sessionId, String title, Long userId) {
        // 查询符合条件的会话对象
        ChatSession chatSession = super.lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId)
                .one();
        // 如果会话记录不存在
        if (chatSession == null) {
            return;
        }
        // 如果会话标题不存在（也就是说第一次生成标题），再更新， blank 判断字符串是否为空串，null或者空格串
        if(StrUtil.isEmpty(chatSession.getTitle()) && StrUtil.isNotBlank(title)) {
            // chatSession.setTitle(StrUtil.sub(title, 0, 20));
            if (ObjectUtil.isNotEmpty(title) && ObjectUtil.isEmpty(chatSession.getTitle())) {
                // 对会话设置标题数据
                // chatSession.setTitle(StrUtil.sub(title, 0, 100));

                // 让AI提炼标题（需要自定义系统提示词，对接AI大模型，进行提炼）
                String userText = StrUtil.format("""
                对话内容：
                {}
                """, title);
                String titleContent = ChatClient.builder(this.dashScopeChatModel)
                        .build()
                        .prompt()
                        .system(this.systemPromptConfig.getChatTitleMessage().get())
                        .user(userText)
                        .call()
                        .content();
                chatSession.setTitle(titleContent);
            }
        }
        // 设置更新时间为当前系统时间
        chatSession.setUpdateTime(LocalDateTime.now());
        // 更新数据库
        super.updateById(chatSession);
    }

    /**
     * 查询历史会话
     *
     * @return
     */
    @Override
    public Map<String, List<ChatSessionVO>> queryHistorySession() {
        // 获取登陆人Id
        Long userId = UserContext.getUser();
        // 查询历史会话，拼接条件
        List<ChatSession> chatSessionList = super.lambdaQuery()
                .eq(ChatSession::getUserId, userId)
                .isNotNull(ChatSession::getTitle)
                .orderByDesc(ChatSession::getUpdateTime)
                .last("LIMIT 30")
                .list();
        // 如果没查到，返回空结果
        if(CollUtil.isEmpty(chatSessionList)) {
            return Map.of();
        }
        // 转成VO
        List<ChatSessionVO> chatSessionVOList = chatSessionList.stream()
                .map(chatSession -> ChatSessionVO.builder()
                        .sessionId(chatSession.getSessionId())
                        .title(chatSession.getTitle())
                        .updateTime(chatSession.getUpdateTime())
                        .build())
                .toList();


        final var TODAY = "当天";
        final var LAST_30_DAYS = "最近30天";
        final var LAST_YEAR = "最近1年";
        final var MORE_THAN_YEAR = "1年以上";

        // 当前时间
        LocalDate now = LocalDateTime.now().toLocalDate();
        Map<String, List<ChatSessionVO>> result = CollStreamUtil.groupByKey(chatSessionVOList, chatSessionVO -> {
            // 计算对应会话日期距今的天数
            long between = Math.abs(ChronoUnit.DAYS.between(chatSessionVO.getUpdateTime().toLocalDate(), now));
            if (between < 1) {
                return TODAY;
            } else if (between < 30) {
                return LAST_30_DAYS;
            } else if (between < 365) {
                return LAST_YEAR;
            } else {
                return MORE_THAN_YEAR;
            }
        });
        return result;
    }

    /**
     * 删除历史会话
     *
     * @param sessionId
     */
    @Override
    public void deleteHistorySession(String sessionId) {
        // 1.先删除数据库
        LambdaQueryWrapper<ChatSession> wrapper = Wrappers.<ChatSession>lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, UserContext.getUser());
        super.remove(wrapper);
        // 2.再删除redis缓存
        String conversationId = ChatService.getConversationId(sessionId);
        redisChatMemory.clear(conversationId);
    }

    /**
     * 更新历史会话标题
     *
     * @param sessionId
     * @param title
     */
    @Override
    public void updateHistorySessionTitle(String sessionId, String title) {
        // 1. 获取当前登录的用户ID
        Long userId = UserContext.getUser();

        // 2. 执行更新：直接在 update 方法中构建 Wrapper
        this.update(
                Wrappers.<ChatSession>lambdaUpdate()
                        .eq(ChatSession::getSessionId, sessionId)
                        // 确保只能修改自己的会话
                        .eq(ChatSession::getUserId, userId)
                        // 更新标题与时间
                        .set(ChatSession::getTitle, StrUtil.sub(title, 0, 100))
                        .set(ChatSession::getUpdateTime, LocalDateTime.now())
        );
    }


}
