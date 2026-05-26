package com.eduverse.aigc.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.eduverse.aigc.agent.AbstractAgent;
import com.eduverse.aigc.agent.Agent;
import com.eduverse.aigc.agent.RouteAgent;
import com.eduverse.aigc.enums.AgentTypeEnum;
import com.eduverse.aigc.enums.ChatEventTypeEnum;
import com.eduverse.aigc.service.ChatService;
import com.eduverse.aigc.vo.ChatEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Map;


/**
 * @author 29410
 */
@Primary
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tj.ai", name = "chat-type", havingValue = "ROUTE")
public class AgentServiceImpl implements ChatService {

    private final RouteAgent routeAgent;

    /**
     * AI助理聊天对话
     *
     * @param question  用户的问题
     * @param sessionId 会话id
     * @return ChatEventVO
     */
    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        // 1.创建路由智能体实例并调用处理方法
        String result = findByAgentType(AgentTypeEnum.ROUTE).process(question, sessionId);
        // 2. 根据路由智能体返回结果查找对应的智能体实例
        Agent agent = findByAgentType(AgentTypeEnum.agentNameOf(result));
        // 3. 如果没有找到智能体实例，则返回自然语言结果
        if (agent == null) {
            ChatEventVO chatEventVO = ChatEventVO.builder()
                    .eventType(ChatEventTypeEnum.DATA.getValue())
                    .eventData(result)
                    .build();
            return Flux.just(chatEventVO, AbstractAgent.STOP_EVENT);
        }
        // 4. 调用对应的智能体，返回结果
        return agent.processStream(question, sessionId);
    }

    /**
     * 根据枚举类型获取对应的智能体实例
     * @param agentType 智能体类型
     * @return 智能体实例
     */
    private static Agent findByAgentType(AgentTypeEnum agentType) {
        // 获取所有类型的智能体实例(spring框架的方法)
        Map<String, Agent> beansOfType = SpringUtil.getBeansOfType(Agent.class);
        // 遍历map，获取对应的智能体实例
        for (Agent agent : beansOfType.values()) {
            if (agent.getAgentType() == agentType) {
                return agent;
            }
        }
        return null;
    }

    /**
     * 停止会话
     *
     * @param sessionId 会话id
     */
    @Override
    public void stop(String sessionId) {
        findByAgentType(AgentTypeEnum.ROUTE).stop(sessionId);
    }
}
