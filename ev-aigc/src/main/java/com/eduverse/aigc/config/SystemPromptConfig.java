package com.eduverse.aigc.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 从 Nacos 配置中心动态加载和监听一个系统提示词（System Prompt）的配置，并将其安全地提供给应用程序的其他部分使用。
 */
@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class SystemPromptConfig {

    // 用于与 Nacos 配置中心进行交互，比如获取配置、监听配置变更等。
    private final NacosConfigManager nacosConfigManager;
    private final AIProperties aiProperties;

    // 使用原子引用，保证线程安全，底层是CAS（Compare-And-Swap）机制，init方法就是将配置加载到chatSystemMessage中
    // 确保在多线程下，只有一个线程能成功更新chatSystemMessage的值
    // 提前加载配置，避免在多线程下，多个线程同时加载配置，并且保证了配置中心没出问题
    private final AtomicReference<String> chatSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> chatTitleMessage = new AtomicReference<>();
    private final AtomicReference<String> routeAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> recommendAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> buyAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> consultAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> knowledgeAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> textSystemMessage = new AtomicReference<>();

    @PostConstruct // 初始化时加载配置
    public void init() {
        // 读取配置文件
        loadConfig(aiProperties.getSystem().getChat(), chatSystemMessage);
        loadConfig(aiProperties.getSystem().getTitle(), chatTitleMessage);
        loadConfig(aiProperties.getSystem().getRouteAgent(), routeAgentSystemMessage);
        loadConfig(aiProperties.getSystem().getRecommendAgent(), recommendAgentSystemMessage);
        loadConfig(aiProperties.getSystem().getBuyAgent(), buyAgentSystemMessage);
        loadConfig(aiProperties.getSystem().getConsultAgent(), consultAgentSystemMessage);
        loadConfig(aiProperties.getSystem().getKnowledgeAgent(), knowledgeAgentSystemMessage);
        loadConfig(aiProperties.getSystem().getText(), textSystemMessage);
    }

    private void loadConfig(AIProperties.System.Chat chatConfig, AtomicReference<String> target) {
        try {
            var dataId = chatConfig.getDataId();
            var group = chatConfig.getGroup();
            var timeoutMs = chatConfig.getTimeoutMs();

            // 1. 主动读取配置
            var config = nacosConfigManager.getConfigService().getConfig(dataId, group, timeoutMs);
            target.set(config);
            log.info("读取{}成功，内容为：{}", target, config);

            // 2. 添加监听器，实现动态更新
            nacosConfigManager.getConfigService().addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null; // 使用 Nacos 客户端的默认线程池
                }

                @Override
                public void receiveConfigInfo(String info) {
                    target.set(info);
                    log.info("更新{}成功，内容为：{}", target, info);
                }
            });
        } catch (Exception e) {
            log.error("加载配置失败", e);
        }
    }

}