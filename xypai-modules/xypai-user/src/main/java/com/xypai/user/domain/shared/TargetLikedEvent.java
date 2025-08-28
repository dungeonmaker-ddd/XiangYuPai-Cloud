package com.xypai.user.domain.shared;

import com.xypai.user.domain.enums.TargetType;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * 目标点赞事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record TargetLikedEvent(
        String eventId,
        TargetId targetId,
        TargetType targetType,
        UserId userId,
        Instant occurredOn
) implements DomainEvent {

    public static TargetLikedEvent create(TargetId targetId, TargetType targetType, UserId userId) {
        return new TargetLikedEvent(
                UUID.randomUUID().toString(),
                targetId,
                targetType,
                userId,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "target.liked";
    }
}
