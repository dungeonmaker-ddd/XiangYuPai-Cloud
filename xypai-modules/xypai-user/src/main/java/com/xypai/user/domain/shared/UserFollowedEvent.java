package com.xypai.user.domain.shared;

import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * 👥 用户关注事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UserFollowedEvent(
        String eventId,
        UserId followerId,
        UserId followeeId,
        Instant occurredOn
) implements DomainEvent {

    public static UserFollowedEvent create(UserId followerId, UserId followeeId) {
        return new UserFollowedEvent(
                UUID.randomUUID().toString(),
                followerId,
                followeeId,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "user.followed";
    }
}
