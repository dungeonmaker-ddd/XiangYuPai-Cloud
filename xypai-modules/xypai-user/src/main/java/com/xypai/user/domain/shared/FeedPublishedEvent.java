package com.xypai.user.domain.shared;

import com.xypai.user.domain.enums.FeedType;
import com.xypai.user.domain.valueobject.FeedId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 动态发布事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record FeedPublishedEvent(
        String eventId,
        FeedId feedId,
        UserId authorId,
        FeedType feedType,
        Instant occurredOn
) implements DomainEvent {

    public FeedPublishedEvent {
        Objects.requireNonNull(eventId, "事件ID不能为空");
        Objects.requireNonNull(feedId, "动态ID不能为空");
        Objects.requireNonNull(authorId, "作者ID不能为空");
        Objects.requireNonNull(feedType, "动态类型不能为空");
        Objects.requireNonNull(occurredOn, "发生时间不能为空");
    }

    public static FeedPublishedEvent create(FeedId feedId, UserId authorId, FeedType feedType) {
        return new FeedPublishedEvent(
                UUID.randomUUID().toString(),
                feedId,
                authorId,
                feedType,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "feed.published";
    }
}
