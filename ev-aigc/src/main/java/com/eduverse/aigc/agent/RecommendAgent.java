package com.eduverse.aigc.agent;

import cn.hutool.core.collection.CollUtil;
import com.eduverse.aigc.config.SystemPromptConfig;
import com.eduverse.aigc.config.ToolResultHolder;
import com.eduverse.aigc.constants.Constant;
import com.eduverse.aigc.enums.AgentTypeEnum;
import com.eduverse.aigc.tools.CourseTools;
import com.eduverse.aigc.tools.result.CourseInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
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

    /**
     *   ┌─────────────────────┬──────────────┬─────────────────────────────────────────────────────────────────────┐
     *   │        参数         │      值      │                                含义                                 │
     *   ├─────────────────────┼──────────────┼─────────────────────────────────────────────────────────────────────┤
     *   │ vectorStore         │ 向量库实例   │ 去哪搜                                                              │
     *   ├─────────────────────┼──────────────┼─────────────────────────────────────────────────────────────────────┤
     *   │ searchRequest.query │ 用户原始消息 │ 拿什么当检索词（QuestionAnswerAdvisor 默认用 userMessage 做 query） │
     *   ├─────────────────────┼──────────────┼─────────────────────────────────────────────────────────────────────┤
     *   │ similarityThreshold │ 0.6          │ 相似度低于 0.6 的就丢弃                                             │
     *   ├─────────────────────┼──────────────┼─────────────────────────────────────────────────────────────────────┤
     *   │ topK                │ 6            │ 最多保留 6 条                                                       │
     *   └─────────────────────┴──────────────┴────────────────────────────────────────────────────────────────────
     */
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
                Constant.REQUEST_ID, requestId
        );
    }

    @Override
    protected void afterStream(String requestId, String sessionId) {
        try {
            var map = ToolResultHolder.get(requestId);
            if (CollUtil.isNotEmpty(map)) {
                return;
            }
            var courses = courseTools.getAvailableCourses();
            if (courses != null && !courses.isEmpty()) {
                var ids = courses.stream().map(CourseInfo::getId).toList();
                courseTools.queryCoursesByIdsInternal(ids, requestId);
            }
        } catch (Exception e) {
            log.warn("afterStream fallback failed, requestId={}", requestId, e);
        }
    }
}