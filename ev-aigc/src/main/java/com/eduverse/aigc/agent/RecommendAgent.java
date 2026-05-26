package com.eduverse.aigc.agent;

import com.eduverse.aigc.config.SystemPromptConfig;
import com.eduverse.aigc.constants.Constant;
import com.eduverse.aigc.enums.AgentTypeEnum;
import com.eduverse.aigc.tools.CourseTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RecommendAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final VectorStore vectorStore;
    private final CourseTools courseTools;

    // 大模型类型
    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.RECOMMEND;
    }

    // 系统提示词
    @Override
    public String systemMessage() {
        return systemPromptConfig.getRecommendAgentSystemMessage().get();
    }

    // RAG检索增强
    @Override
    public List<Advisor> advisors() {
        var qaAdvisor = QuestionAnswerAdvisor.builder(this.vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.6d).topK(6).build())
                .build();
        return List.of(qaAdvisor);
    }

    // tool工具
    @Override
    public Object[] tools() {
        return new Object[]{courseTools};
    }

    // 给工具传递参数
    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        return Map.of(
                // 设置请求id参数
                Constant.REQUEST_ID, requestId
        );
    }
}