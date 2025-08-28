package com.xypai.user.domain.shared;

import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * 👤 用户创建事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UserCreatedEvent(
        String eventId,
        UserId userId,
        String mobile,
        String nickname,
        String clientType,
        Instant occurredOn
) implements DomainEvent {

    public static UserCreatedEvent create(
            UserId userId,
            String mobile,
            String nickname,
            String clientType
    ) {
        return new UserCreatedEvent(
                UUID.randomUUID().toString(),
                userId,
                mobile,
                nickname,
                clientType,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "user.created";
    }
}
