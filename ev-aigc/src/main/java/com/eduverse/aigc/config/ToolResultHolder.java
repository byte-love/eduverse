package com.eduverse.aigc.config;

import cn.hutool.core.lang.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具结果保持器，用来存储tools中得到的结果，请求id 作为key， value为键值对数据
 * 主要是用于将结果交给前端让前端展示一张卡片给用户看
 * 大模型接收到的数据流式输出给了用户，而前端要制作卡片的话必须要统一的格式结果
 * 这个结果暂存类就保存了前端需要的结果格式
 *
 * @author 
 * @version 1.0
 */
public class ToolResultHolder {

    private static final Map<String, Map<String, Object>> HANDLER_MAP = new ConcurrentHashMap<>();


    /**
     * 工具类，禁止实例化
     */
    private ToolResultHolder() {
    }

    public static void put(String key, String field, Object result) {
        Assert.notNull(key, "key is not null!");
        Assert.notNull(field, "field is not null!");
        HANDLER_MAP.computeIfAbsent(key, k -> new HashMap<>()).put(field, result);
    }

    public static Map<String, Object> get(String key) {
        return key == null ? null : HANDLER_MAP.get(key);
    }

    public static Object get(String key, String field) {
        Assert.notNull(key, "key is not null!");
        Assert.notNull(field, "field is not null!");
        return Optional.ofNullable(HANDLER_MAP.get(key))
                .map(map -> map.get(field))
                .orElse(null);
    }

    public static void remove(String key) {
        Assert.notNull(key, "key is not null!");
        HANDLER_MAP.remove(key);
    }

}