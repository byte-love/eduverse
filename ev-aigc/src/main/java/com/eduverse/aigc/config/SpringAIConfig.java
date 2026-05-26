package com.eduverse.aigc.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.eduverse.aigc.advisor.RecordOptimizationAdvisor;
import com.eduverse.aigc.memory.MyChatMemoryRepository;
import com.eduverse.aigc.memory.RedisChatMemoryRepository;
import com.eduverse.aigc.tools.CourseTools;
import com.eduverse.aigc.tools.OrderTools;
import com.eduverse.common.constants.Constant;
import com.eduverse.common.utils.WebUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class SpringAIConfig {

    @Value("${tj.ai.memory.max:100}")
    private Integer maxMessages;

    /**
     * 创建ChatClient
     */
    @Bean
    public ChatClient chatClient(DashScopeChatModel dashScopeChatModel,
                                 ChatMemory redisChatMemory,
                                 CourseTools courseTools,
                                 OrderTools orderTools
    ) {
        return ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(
                        // 添加日志记录advisor
                        SimpleLoggerAdvisor.builder().build(),
                        // 添加会话记忆advisor
                        MessageChatMemoryAdvisor.builder(redisChatMemory).build()
                )
                // 添加工具
                .defaultTools(courseTools, orderTools)
                .build();
    }



    /**
     * 创建ChatClient
     */
    @Bean
    public ChatClient agentChatClient(
            DashScopeChatModel dashScopeChatModel,
            ChatMemory redisChatMemory,
            Advisor recordOptimizationAdvisor
            ) {
        return ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(
                        // 添加日志记录advisor
                        SimpleLoggerAdvisor.builder().build(),
                        // 会话记忆advisor
                        MessageChatMemoryAdvisor.builder(redisChatMemory).build(),
                        // 记录优化advisor
                        recordOptimizationAdvisor
                )
                .build();
    }


    // 创建自定义Redis会话记忆存储仓库
    @Bean
    public ChatMemoryRepository redisChatMemoryRepository() {
        return new RedisChatMemoryRepository();
    }

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel, Advisor loggerAdvisor) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(loggerAdvisor)
                .build();
    }

    // 创建自定义记录优化器
    @Bean
    public Advisor recordOptimizationAdvisor(MyChatMemoryRepository myChatMemoryRepository) {
        return new RecordOptimizationAdvisor(myChatMemoryRepository);
    }

    // 创建Redis会话记忆管理器
    @Bean
    public ChatMemory redisChatMemory(ChatMemoryRepository redisChatMemoryRepository) {
        // 基于 chatMemoryRepository 对象构建 chatMemory 对象
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                // 最多保存 100 条对话, 如果超出的话，会自动删除最旧的对话
                .maxMessages(this.maxMessages)
                .build();
    }

    /**
     * 创建并配置自定义重试监听器Bean
     * <p>
     * 实现说明：
     * 1. 创建匿名RetryListener实现，在重试操作期间管理Web属性
     * 2. 将监听器注册到提供的RetryTemplate实例
     * @param retryTemplate Spring Retry模板对象，用于注册重试监听器
     * @return RetryListener 已注册到模板的重试监听器实例，将由Spring容器管理
     */
    @Bean
    public RetryListener customizeRetryTemplate(RetryTemplate retryTemplate) {
        // 创建自定义重试监听器，实现以下核心功能：
        // - 重试开始时设置上下文标识
        // - 重试结束后清理上下文标识
        RetryListener retryListener = new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                WebUtils.setAttribute(Constant.SPRING_AI_ATTR, Constant.SPRING_AI_FLAG);
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                WebUtils.removeAttribute(Constant.SPRING_AI_ATTR);
            }
        };

        // 将监听器注册到重试模板
        retryTemplate.registerListener(retryListener);
        return retryListener;
    }



}