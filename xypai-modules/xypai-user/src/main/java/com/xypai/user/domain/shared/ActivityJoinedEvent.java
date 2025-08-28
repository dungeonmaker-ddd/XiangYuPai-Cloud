package com.xypai.user.domain.shared;

import com.xypai.user.domain.entity.ActivityParticipant;
import com.xypai.user.domain.valueobject.ActivityId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 🎉 活动参与事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record ActivityJoinedEvent(
        String eventId,
        ActivityId activityId,
        UserId participantId,
        ActivityParticipant.ParticipantStatus status,
        Instant occurredOn
) implements DomainEvent {

    public ActivityJoinedEvent {
        Objects.requireNonNull(eventId, "事件ID不能为空");
        Objects.requireNonNull(activityId, "活动ID不能为空");
        Objects.requireNonNull(participantId, "参与者ID不能为空");
        Objects.requireNonNull(status, "参与状态不能为空");
        Objects.requireNonNull(occurredOn, "发生时间不能为空");
    }

    /**
     * 静态工厂方法：创建活动参与事件
     */
    public static ActivityJoinedEvent create(ActivityId activityId, UserId participantId, ActivityParticipant.ParticipantStatus status) {
        return new ActivityJoinedEvent(
                UUID.randomUUID().toString(),
                activityId,
                participantId,
                status,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "activity.joined";
    }
}
