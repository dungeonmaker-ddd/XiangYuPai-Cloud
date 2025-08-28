package com.xypai.user.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.UUID;

/**
 * 🤝 社交ID值对象
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record SocialId(@JsonValue String value) {

    public SocialId {
        Objects.requireNonNull(value, "社交ID不能为空");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("社交ID不能为空字符串");
        }
    }

    /**
     * 生成新的社交ID
     */
    public static SocialId generate() {
        return new SocialId(UUID.randomUUID().toString());
    }

    /**
     * 从字符串创建社交ID
     */
    public static SocialId of(String value) {
        return new SocialId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
