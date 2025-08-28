package com.xypai.user.domain.interaction.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * 目标对象ID值对象
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record TargetId(@JsonValue String value) {

    public TargetId {
        Objects.requireNonNull(value, "目标ID不能为空");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("目标ID不能为空字符串");
        }
    }

    /**
     * 从字符串创建目标ID
     */
    public static TargetId of(String value) {
        return new TargetId(value);
    }

    /**
     * 从Long创建目标ID
     */
    public static TargetId of(Long value) {
        Objects.requireNonNull(value, "目标ID不能为空");
        return new TargetId(String.valueOf(value));
    }

    @Override
    public String toString() {
        return value;
    }
}
