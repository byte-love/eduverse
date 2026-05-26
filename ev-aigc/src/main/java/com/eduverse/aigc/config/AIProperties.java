package com.eduverse.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tj.ai.prompt")
public class AIProperties {

    // 系统提示语，用于课程推荐、购买业务
    private System system;

    @Data
    public static class System {
        private Chat chat;
        private Chat title;
        // 路由智能体系统提示词
        private Chat routeAgent;
        // 推荐智能体系统提示词
        private Chat recommendAgent;
        // 购买智能体系统提示词
        private Chat buyAgent;
        // 咨询智能体系统提示词
        private Chat consultAgent;
        // 知识讲解智能体系统提示词
        private Chat knowledgeAgent;
        // 文本提示语，用于问答回复、润色等文本类型的业务
        private Chat text;

        @Data
        public static class Chat {
            private String dataId;
            private String group = "DEFAULT_GROUP";
            private long timeoutMs = 20000L; // 读取的超时时间，单位毫秒
        }
    }
}