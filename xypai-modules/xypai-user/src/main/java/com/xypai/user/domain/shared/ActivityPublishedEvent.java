package com.xypai.user.domain.shared;

import com.xypai.user.domain.valueobject.ActivityId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 📱 活动发布事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record ActivityPublishedEvent(
        String eventId,
        ActivityId activityId,
        UserId organizerId,
        LocalDateTime startTime,
        Instant occurredOn
) implements DomainEvent {

    public ActivityPublishedEvent {
        Objects.requireNonNull(eventId, "事件ID不能为空");
        Objects.requireNonNull(activityId, "活动ID不能为空");
        Objects.requireNonNull(organizerId, "组织者ID不能为空");
        Objects.requireNonNull(startTime, "开始时间不能为空");
        Objects.requireNonNull(occurredOn, "发生时间不能为空");
    }

    /**
     * 静态工厂方法：创建活动发布事件
     */
    public static ActivityPublishedEvent create(ActivityId activityId, UserId organizerId, LocalDateTime startTime) {
        return new ActivityPublishedEvent(
                UUID.randomUUID().toString(),
                activityId,
                organizerId,
                startTime,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "activity.published";
    }
}
