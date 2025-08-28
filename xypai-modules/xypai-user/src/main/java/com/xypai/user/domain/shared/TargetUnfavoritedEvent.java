package com.xypai.user.domain.shared;

import com.xypai.user.domain.enums.TargetType;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * 目标取消收藏事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record TargetUnfavoritedEvent(
        String eventId,
        TargetId targetId,
        TargetType targetType,
        UserId userId,
        Instant occurredOn
) implements DomainEvent {

    public static TargetUnfavoritedEvent create(TargetId targetId, TargetType targetType, UserId userId) {
        return new TargetUnfavoritedEvent(
                UUID.randomUUID().toString(),
                targetId,
                targetType,
                userId,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "target.unfavorited";
    }
}
