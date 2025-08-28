package com.xypai.user.domain.shared;

import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * 👥 用户取消关注事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UserUnfollowedEvent(
        String eventId,
        UserId followerId,
        UserId followeeId,
        Instant occurredOn
) implements DomainEvent {

    public static UserUnfollowedEvent create(UserId followerId, UserId followeeId) {
        return new UserUnfollowedEvent(
                UUID.randomUUID().toString(),
                followerId,
                followeeId,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "user.unfollowed";
    }
}
