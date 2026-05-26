package com.eduverse.aigc.agent;

import com.eduverse.aigc.config.SystemPromptConfig;
import com.eduverse.aigc.constants.Constant;
import com.eduverse.aigc.enums.AgentTypeEnum;
import com.eduverse.aigc.tools.OrderTools;
import com.eduverse.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BuyAgent extends AbstractAgent{

    private final SystemPromptConfig systemPromptConfig;
    private final OrderTools orderTools;

    // 大模型类型
    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.BUY;
    }


    // 系统提示词
    @Override
    public String systemMessage() {
        return systemPromptConfig.getBuyAgentSystemMessage().get();
    }

    // tool工具
    @Override
    public Object[] tools() {
        return new Object[]{orderTools};
    }

    // 给工具传递参数
    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        return Map.of(
                Constant.USER_ID, UserContext.getUser(),// 设置用户id参数
                Constant.REQUEST_ID, requestId  // 设置请求id参数
        );
    }
}