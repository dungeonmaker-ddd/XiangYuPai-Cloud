package com.xypai.user.domain.feed.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.UUID;

/**
 * 动态ID值对象
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record FeedId(@JsonValue String value) {

    public FeedId {
        Objects.requireNonNull(value, "动态ID不能为空");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("动态ID不能为空字符串");
        }
    }

    /**
     * 生成新的动态ID
     */
    public static FeedId generate() {
        return new FeedId(UUID.randomUUID().toString());
    }

    /**
     * 从字符串创建动态ID
     */
    public static FeedId of(String value) {
        return new FeedId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
