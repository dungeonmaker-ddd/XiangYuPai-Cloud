package com.xypai.user.domain.shared;

import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * 👤 用户更新事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UserUpdatedEvent(
        String eventId,
        UserId userId,
        String updateType,
        String oldValue,
        String newValue,
        Instant occurredOn
) implements DomainEvent {

    public static UserUpdatedEvent create(
            UserId userId,
            String updateType,
            String oldValue,
            String newValue
    ) {
        return new UserUpdatedEvent(
                UUID.randomUUID().toString(),
                userId,
                updateType,
                oldValue,
                newValue,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "user.updated";
    }
}
