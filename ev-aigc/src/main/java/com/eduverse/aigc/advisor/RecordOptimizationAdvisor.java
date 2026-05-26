package com.eduverse.aigc.advisor;

import cn.hutool.core.convert.Convert;
import com.eduverse.aigc.enums.AgentTypeEnum;
import com.eduverse.aigc.memory.MyChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

/**
 * @author 29410
 */
public class RecordOptimizationAdvisor implements BaseAdvisor {

    private final MyChatMemoryRepository myChatMemoryRepository;

    public RecordOptimizationAdvisor(MyChatMemoryRepository myChatMemoryRepository) {
        this.myChatMemoryRepository = myChatMemoryRepository;
    }

    // 在会话记忆保存前不需要进行增强，直接返回
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return chatClientRequest;
    }

    // 拦截大模型返回的文本数据，如果返回的文本数据中包含agentName，则进行记录优化
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        var response = chatClientResponse.chatResponse();
        // 获取大模型返回的文本数据，如果返回的文本数据中包含agentName（例如BUY购买智能体），则进行记录优化
        assert response != null;
        var text = response.getResult().getOutput().getText();
        var agentTypeEnum = AgentTypeEnum.agentNameOf(text);
        if (null != agentTypeEnum) {
            // 如果存在则删最近两条会话记录（详情看智能体协同工作3.1因为用户问一句，（路由）智能体返回一个智能体类型一句）
            var key = ChatMemory.CONVERSATION_ID;
            var conversationId = Convert.toStr(chatClientResponse.context().get(key));
            this.myChatMemoryRepository.optimization(conversationId);
        }
        // 如果不是路由智能体，则返回正常结果，不进行记录优化
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        // 优先级比ChatMemoryAdvisor高，这样增强方法先进栈，后执行，确保会话已经保存后才去进行删除
        return Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 100;
    }
}