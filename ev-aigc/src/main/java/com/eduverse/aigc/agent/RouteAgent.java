package com.eduverse.aigc.agent;

import com.eduverse.aigc.config.SystemPromptConfig;
import com.eduverse.aigc.enums.AgentTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 意图分析（路由）智能体
 */
@Component
@RequiredArgsConstructor
public class RouteAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;

    /**
     * 获取智能体的系统提示语
     * @return
     */
    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getRouteAgentSystemMessage().get();
    }

    /**
     * 获取智能体的类型
     * @return
     */
    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.ROUTE;
    }

}