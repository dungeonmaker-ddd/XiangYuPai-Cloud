package com.xypai.user.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * 👤 用户ID值对象
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UserId(@JsonValue Long value) {

    public UserId {
        Objects.requireNonNull(value, "用户ID不能为空");
        if (value <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
    }

    /**
     * 创建用户ID
     */
    public static UserId of(Long value) {
        return new UserId(value);
    }

    /**
     * 创建用户ID
     */
    public static UserId of(String value) {
        Objects.requireNonNull(value, "用户ID字符串不能为空");
        try {
            return new UserId(Long.parseLong(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的用户ID格式: " + value, e);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
