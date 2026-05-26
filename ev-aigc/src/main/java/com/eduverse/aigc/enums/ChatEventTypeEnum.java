package com.eduverse.aigc.enums;

import com.eduverse.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 聊天消息事件类型
 */
@Getter
public enum ChatEventTypeEnum implements BaseEnum {
    DATA(1001, "数据事件"),
    STOP(1002, "停止事件"),
    PARAM(1003, "参数事件");

    private final int value;
    private final String desc;

    ChatEventTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.name();
    }
}