package com.xypai.user.domain.activity.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * 活动ID值对象
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record ActivityId(String value) {

    public ActivityId {
        Objects.requireNonNull(value, "活动ID不能为空");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("活动ID不能为空字符串");
        }
    }

    /**
     * 创建活动ID
     */
    public static ActivityId of(String value) {
        return new ActivityId(value);
    }

    /**
     * 生成新的活动ID
     */
    public static ActivityId generate() {
        return new ActivityId("activity_" + UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * 从长整型ID创建
     */
    public static ActivityId of(Long id) {
        Objects.requireNonNull(id, "ID不能为空");
        return new ActivityId("activity_" + id);
    }
}
