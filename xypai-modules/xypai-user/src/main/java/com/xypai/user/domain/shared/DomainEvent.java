package com.xypai.user.domain.shared;

import java.time.Instant;

/**
 * 🔔 领域事件基础接口
 *
 * @author XyPai
 * @since 2025-01-02
 */
public sealed interface DomainEvent permits
        UserCreatedEvent,
        UserUpdatedEvent,
        UserFollowedEvent,
        UserUnfollowedEvent,
        TargetLikedEvent,
        TargetUnlikedEvent,
        TargetFavoritedEvent,
        TargetUnfavoritedEvent,
        ActivityCreatedEvent,
        ActivityJoinedEvent,
        ActivityPublishedEvent,
        FeedPublishedEvent,
        WalletTransferredEvent,
        WalletRechargedEvent {

    /**
     * 事件唯一标识
     */
    String eventId();

    /**
     * 事件发生时间
     */
    Instant occurredOn();

    /**
     * 事件类型
     */
    String eventType();

    /**
     * 事件版本
     */
    default String version() {
        return "1.0";
    }
}
