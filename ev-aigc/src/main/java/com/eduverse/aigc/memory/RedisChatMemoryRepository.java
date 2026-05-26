package com.eduverse.aigc.memory;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author 29410
 */
@Component
// 基于Redis实现ChatMemoryRepository
public class RedisChatMemoryRepository implements ChatMemoryRepository, MyChatMemoryRepository {

    // 默认redis中key的前缀
    public static final String DEFAULT_PREFIX = "CHAT:";

    // 注入redis模板，进行redis的操作
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 查询所有的对话id
    @Override
    public List<String> findConversationIds() {
        Set<String> keys = stringRedisTemplate.keys(DEFAULT_PREFIX + "*");
        return ListUtil.toList(keys);
    }

    // 根据对话id查询Message列表
    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 获取redis的key
        String redisKey = getKey(conversationId);
        // 获取 Redis 的 List 列表操作对象
        var listOps = stringRedisTemplate.opsForList();
        // 取出所有的消息内容
        List<String> jsonList = listOps.range(redisKey, 0, -1);
        // 将json字符串转换为Message对象
        return CollStreamUtil.toList(jsonList, MessageUtil::toMessage);
    }

    // 保存Message列表数据
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        //1. Hutool 的断言工具，确保 messages 不为 null 或空，否则抛出异常。
        Assert.notEmpty(messages, "消息列表不能为空");
        //2. 拼接Redis的Key
        String redisKey = getKey(conversationId);
        //3. 获取 Redis的List列表操作对象
        var listOps = stringRedisTemplate.opsForList();
        //4. 保存数据时，会传入全部的消息数据，包括之前的数据，所以需要先删除之前的数据，再添加新的数据
        deleteByConversationId(conversationId);
        //5. 将序列化后的字符串推入 Redis List 的右侧（尾部）
        messages.forEach(message -> listOps.rightPush(redisKey, MessageUtil.toJson(message)));
    }

    // 根据对话id删除所有历史消息
    @Override
    public void deleteByConversationId(String conversationId) {
        String redisKey = getKey(conversationId);
        stringRedisTemplate.delete(redisKey);
    }

    // 一个私有工具方法，根据前缀和对话ID生成完整的 Redis Key
    private String getKey(String conversationId) {
        return DEFAULT_PREFIX + conversationId;
    }

    /**
     * 根据对话ID优化对话记录，删除最后的2条消息，因为这2条消息是从路由智能体存储的，请求由后续的智能体处理
     * 为了确保历史消息的完整性，所以需要将中间转发的消息清理掉
     *
     * @param conversationId 对话的唯一标识符
     */
    @Override
    public void optimization(String conversationId) {
        var redisKey = this.getKey(conversationId);
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
        // 从Redis列表右侧弹出2个元素
        listOps.rightPop(2);
    }
}