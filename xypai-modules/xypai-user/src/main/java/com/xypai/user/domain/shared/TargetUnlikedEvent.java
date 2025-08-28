package com.xypai.user.domain.shared;

import com.xypai.user.domain.interaction.enums.TargetType;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * 目标取消点赞事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record TargetUnlikedEvent(
        String eventId,
        TargetId targetId,
        TargetType targetType,
        UserId userId,
        Instant occurredOn
) implements DomainEvent {

    public static TargetUnlikedEvent create(TargetId targetId, TargetType targetType, UserId userId) {
        return new TargetUnlikedEvent(
                UUID.randomUUID().toString(),
                targetId,
                targetType,
                userId,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "target.unliked";
    }
}
