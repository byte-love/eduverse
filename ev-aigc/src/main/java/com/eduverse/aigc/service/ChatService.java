package com.eduverse.aigc.service;

import com.eduverse.aigc.vo.ChatEventVO;
import com.eduverse.common.utils.UserContext;
import reactor.core.publisher.Flux;

public interface ChatService {


    /**
     * AI助理聊天对话
     * @param question  用户的问题
     * @param sessionId 会话id
     * @return ChatEventVO
     */
    public Flux<ChatEventVO> chat(String question, String sessionId);

    /**
     * 停止会话
     * @param sessionId 会话id
     */
    void stop(String sessionId);

    /**
     * 获取对话id
     * @param sessionId 会话id
     * @return
     */
    static String getConversationId(String sessionId) {
        return UserContext.getUser() + "_" + sessionId;
    }
}
