package com.xypai.user.infrastructure.event;

import com.xypai.user.domain.shared.*;
import com.xypai.user.infrastructure.cache.FeedTimelineCacheService;
import com.xypai.user.infrastructure.cache.InteractionCacheService;
import com.xypai.user.infrastructure.cache.SocialGraphCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 缓存事件监听器 - 监听领域事件并更新相关缓存
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEventListener {

    private final SocialGraphCacheService socialGraphCacheService;
    private final FeedTimelineCacheService feedTimelineCacheService;
    private final InteractionCacheService interactionCacheService;

    // ========================================
    // 社交事件处理
    // ========================================

    /**
     * 处理用户关注事件
     */
    @Async
    @EventListener
    public void handleUserFollowedEvent(UserFollowedEvent event) {
        log.info("处理用户关注事件: {} -> {}", event.followerId(), event.followeeId());

        try {
            // 更新社交图缓存
            socialGraphCacheService.addFollowing(event.followerId(), event.followeeId());

            // 清除社交统计缓存（触发重新计算）
            socialGraphCacheService.invalidateSocialStats(event.followerId());
            socialGraphCacheService.invalidateSocialStats(event.followeeId());

            // 清除关注者的时间线缓存（需要重新包含被关注者的动态）
            feedTimelineCacheService.invalidateFollowingTimeline(event.followerId());

        } catch (Exception e) {
            log.error("处理用户关注事件失败", e);
        }
    }

    /**
     * 处理用户取消关注事件
     */
    @Async
    @EventListener
    public void handleUserUnfollowedEvent(UserUnfollowedEvent event) {
        log.info("处理用户取消关注事件: {} -> {}", event.followerId(), event.followeeId());

        try {
            // 更新社交图缓存
            socialGraphCacheService.removeFollowing(event.followerId(), event.followeeId());

            // 清除社交统计缓存
            socialGraphCacheService.invalidateSocialStats(event.followerId());
            socialGraphCacheService.invalidateSocialStats(event.followeeId());

            // 清除关注者的时间线缓存
            feedTimelineCacheService.invalidateFollowingTimeline(event.followerId());

        } catch (Exception e) {
            log.error("处理用户取消关注事件失败", e);
        }
    }

    // ========================================
    // 动态事件处理
    // ========================================

    /**
     * 处理动态发布事件
     */
    @Async
    @EventListener
    public void handleFeedPublishedEvent(FeedPublishedEvent event) {
        log.info("处理动态发布事件: feedId={}, authorId={}", event.feedId(), event.authorId());

        try {
            // 添加到作者的时间线缓存
            feedTimelineCacheService.addToUserTimelineHead(event.authorId(), event.feedId());

            // TODO: 获取作者的粉丝列表，推送到粉丝的关注时间线
            // 这里需要调用社交服务获取粉丝列表
            // List<UserId> followers = socialService.getFollowers(event.authorId());
            // feedTimelineCacheService.pushToFollowersTimeline(followers, event.feedId());

            // 如果是高质量动态，添加到热门列表
            if (isHighQualityFeed(event)) {
                feedTimelineCacheService.addToHotFeeds(event.feedId());
            }

        } catch (Exception e) {
            log.error("处理动态发布事件失败", e);
        }
    }

    // ========================================
    // 互动事件处理
    // ========================================

    /**
     * 处理目标点赞事件
     */
    @Async
    @EventListener
    public void handleTargetLikedEvent(TargetLikedEvent event) {
        log.info("处理目标点赞事件: targetId={}, userId={}", event.targetId(), event.userId());

        try {
            // 更新互动统计缓存
            interactionCacheService.incrementLikeCount(event.targetId());

            // 缓存用户点赞关系
            interactionCacheService.cacheUserLikeTarget(event.userId(), event.targetId(), true);

            // 添加到用户点赞列表
            interactionCacheService.addToUserLikes(event.userId(), event.targetId());

            // 清除目标的详细互动统计缓存（触发重新计算）
            interactionCacheService.invalidateInteractionStats(event.targetId());

        } catch (Exception e) {
            log.error("处理目标点赞事件失败", e);
        }
    }

    /**
     * 处理目标取消点赞事件
     */
    @Async
    @EventListener
    public void handleTargetUnlikedEvent(TargetUnlikedEvent event) {
        log.info("处理目标取消点赞事件: targetId={}, userId={}", event.targetId(), event.userId());

        try {
            // 缓存用户点赞关系
            interactionCacheService.cacheUserLikeTarget(event.userId(), event.targetId(), false);

            // 清除相关统计缓存
            interactionCacheService.invalidateInteractionStats(event.targetId());

            // 清除用户点赞列表缓存（触发重新加载）
            interactionCacheService.invalidateUserInteractionCache(event.userId());

        } catch (Exception e) {
            log.error("处理目标取消点赞事件失败", e);
        }
    }

    /**
     * 处理目标收藏事件
     */
    @Async
    @EventListener
    public void handleTargetFavoritedEvent(TargetFavoritedEvent event) {
        log.info("处理目标收藏事件: targetId={}, userId={}", event.targetId(), event.userId());

        try {
            // 更新互动统计缓存
            interactionCacheService.incrementFavoriteCount(event.targetId());

            // 缓存用户收藏关系
            interactionCacheService.cacheUserFavoriteTarget(event.userId(), event.targetId(), true);

            // 添加到用户收藏列表
            interactionCacheService.addToUserFavorites(event.userId(), event.targetId());

            // 清除目标的详细互动统计缓存
            interactionCacheService.invalidateInteractionStats(event.targetId());

        } catch (Exception e) {
            log.error("处理目标收藏事件失败", e);
        }
    }

    /**
     * 处理目标取消收藏事件
     */
    @Async
    @EventListener
    public void handleTargetUnfavoritedEvent(TargetUnfavoritedEvent event) {
        log.info("处理目标取消收藏事件: targetId={}, userId={}", event.targetId(), event.userId());

        try {
            // 缓存用户收藏关系
            interactionCacheService.cacheUserFavoriteTarget(event.userId(), event.targetId(), false);

            // 清除相关统计缓存
            interactionCacheService.invalidateInteractionStats(event.targetId());

            // 清除用户收藏列表缓存
            interactionCacheService.invalidateUserInteractionCache(event.userId());

        } catch (Exception e) {
            log.error("处理目标取消收藏事件失败", e);
        }
    }

    // ========================================
    // 用户事件处理
    // ========================================

    /**
     * 处理用户创建事件
     */
    @Async
    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("处理用户创建事件: userId={}", event.userId());

        try {
            // 预热用户基础缓存
            // 这里可以预设一些默认的缓存数据

        } catch (Exception e) {
            log.error("处理用户创建事件失败", e);
        }
    }

    /**
     * 处理用户更新事件
     */
    @Async
    @EventListener
    public void handleUserUpdatedEvent(UserUpdatedEvent event) {
        log.info("处理用户更新事件: userId={}, field={}", event.userId(), event.updateType());

        try {
            // 清除用户相关缓存
            socialGraphCacheService.invalidateUserSocialCache(event.userId());

        } catch (Exception e) {
            log.error("处理用户更新事件失败", e);
        }
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 判断是否为高质量动态
     */
    private boolean isHighQualityFeed(FeedPublishedEvent event) {
        // 这里可以根据作者的影响力、动态类型等因素判断
        // 简单示例：图片和视频动态视为高质量
        return event.feedType().isMultimedia();
    }
}
