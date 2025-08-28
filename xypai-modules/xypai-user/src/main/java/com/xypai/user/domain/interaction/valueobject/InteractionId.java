package com.xypai.user.domain.interaction.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.UUID;

/**
 * 互动ID值对象
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record InteractionId(@JsonValue String value) {

    public InteractionId {
        Objects.requireNonNull(value, "互动ID不能为空");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("互动ID不能为空字符串");
        }
    }

    /**
     * 生成新的互动ID
     */
    public static InteractionId generate() {
        return new InteractionId(UUID.randomUUID().toString());
    }

    /**
     * 从字符串创建互动ID
     */
    public static InteractionId of(String value) {
        return new InteractionId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
