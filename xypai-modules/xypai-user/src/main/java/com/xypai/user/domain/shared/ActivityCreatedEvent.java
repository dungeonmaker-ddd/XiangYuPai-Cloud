package com.xypai.user.domain.shared;

import com.xypai.user.domain.activity.enums.ActivityType;
import com.xypai.user.domain.valueobject.ActivityId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 📱 活动创建事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record ActivityCreatedEvent(
        String eventId,
        ActivityId activityId,
        UserId organizerId,
        ActivityType activityType,
        Instant occurredOn
) implements DomainEvent {

    public ActivityCreatedEvent {
        Objects.requireNonNull(eventId, "事件ID不能为空");
        Objects.requireNonNull(activityId, "活动ID不能为空");
        Objects.requireNonNull(organizerId, "组织者ID不能为空");
        Objects.requireNonNull(activityType, "活动类型不能为空");
        Objects.requireNonNull(occurredOn, "发生时间不能为空");
    }

    /**
     * 静态工厂方法：创建活动创建事件
     */
    public static ActivityCreatedEvent create(ActivityId activityId, UserId organizerId, ActivityType activityType) {
        return new ActivityCreatedEvent(
                UUID.randomUUID().toString(),
                activityId,
                organizerId,
                activityType,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "activity.created";
    }
}
