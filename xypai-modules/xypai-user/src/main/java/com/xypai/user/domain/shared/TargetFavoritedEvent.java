package com.xypai.user.domain.shared;

import com.xypai.user.domain.interaction.enums.TargetType;
import com.xypai.user.domain.user.valueobject.UserId;
import com.xypai.user.domain.valueobject.TargetId;

import java.time.Instant;
import java.util.UUID;

/**
 * 目标收藏事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record TargetFavoritedEvent(
        String eventId,
        TargetId targetId,
        TargetType targetType,
        UserId userId,
        Instant occurredOn
) implements DomainEvent {

    public static TargetFavoritedEvent create(TargetId targetId, TargetType targetType, UserId userId) {
        return new TargetFavoritedEvent(
                UUID.randomUUID().toString(),
                targetId,
                targetType,
                userId,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "target.favorited";
    }
}
