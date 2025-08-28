package com.xypai.user.infrastructure.cache;

import com.xypai.user.application.service.InteractionApplicationService;
import com.xypai.user.domain.service.CacheService;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 互动统计缓存服务 - 专门处理点赞、收藏、评论等互动数据的缓存
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionCacheService {

    // 缓存过期时间配置
    private static final Duration INTERACTION_STATS_TTL = Duration.ofMinutes(10); // 互动统计缓存10分钟
    private static final Duration USER_INTERACTION_TTL = Duration.ofMinutes(5);   // 用户互动关系缓存5分钟
    private static final Duration USER_LIKES_TTL = Duration.ofHours(1);          // 用户点赞列表缓存1小时
    private static final Duration USER_FAVORITES_TTL = Duration.ofHours(2);      // 用户收藏列表缓存2小时
    private final CacheService cacheService;

    // ========================================
    // 互动统计缓存
    // ========================================

    /**
     * 缓存目标互动统计
     */
    public void cacheInteractionStats(TargetId targetId, InteractionApplicationService.InteractionStatus stats) {
        String key = CacheKeys.interactionStats(targetId);
        cacheService.set(key, stats, INTERACTION_STATS_TTL);
        log.debug("缓存互动统计: targetId={}, likes={}, favorites={}, comments={}",
                targetId, stats.likeCount(), stats.favoriteCount(), stats.commentCount());
    }

    /**
     * 获取目标互动统计缓存
     */
    public Optional<InteractionApplicationService.InteractionStatus> getInteractionStats(TargetId targetId) {
        String key = CacheKeys.interactionStats(targetId);
        return cacheService.get(key, InteractionApplicationService.InteractionStatus.class);
    }

    /**
     * 更新点赞统计缓存
     */
    public void incrementLikeCount(TargetId targetId) {
        String key = CacheKeys.interactionStats(targetId) + ":likes";
        long newCount = cacheService.increment(key);
        log.debug("递增点赞统计: targetId={}, newCount={}", targetId, newCount);
    }

    /**
     * 更新收藏统计缓存
     */
    public void incrementFavoriteCount(TargetId targetId) {
        String key = CacheKeys.interactionStats(targetId) + ":favorites";
        long newCount = cacheService.increment(key);
        log.debug("递增收藏统计: targetId={}, newCount={}", targetId, newCount);
    }

    /**
     * 更新评论统计缓存
     */
    public void incrementCommentCount(TargetId targetId) {
        String key = CacheKeys.interactionStats(targetId) + ":comments";
        long newCount = cacheService.increment(key);
        log.debug("递增评论统计: targetId={}, newCount={}", targetId, newCount);
    }

    /**
     * 清除目标互动统计缓存
     */
    public void invalidateInteractionStats(TargetId targetId) {
        String key = CacheKeys.interactionStats(targetId);
        cacheService.delete(key);

        // 同时清除分项统计
        cacheService.delete(key + ":likes");
        cacheService.delete(key + ":favorites");
        cacheService.delete(key + ":comments");

        log.debug("清除互动统计缓存: targetId={}", targetId);
    }

    // ========================================
    // 用户互动关系缓存
    // ========================================

    /**
     * 缓存用户点赞关系
     */
    public void cacheUserLikeTarget(UserId userId, TargetId targetId, boolean isLiked) {
        String key = CacheKeys.userLikeTarget(userId, targetId);
        cacheService.set(key, isLiked, USER_INTERACTION_TTL);
        log.debug("缓存用户点赞关系: userId={}, targetId={}, isLiked={}", userId, targetId, isLiked);
    }

    /**
     * 获取用户点赞关系缓存
     */
    public Optional<Boolean> getUserLikeTarget(UserId userId, TargetId targetId) {
        String key = CacheKeys.userLikeTarget(userId, targetId);
        return cacheService.get(key, Boolean.class);
    }

    /**
     * 缓存用户收藏关系
     */
    public void cacheUserFavoriteTarget(UserId userId, TargetId targetId, boolean isFavorited) {
        String key = CacheKeys.userFavoriteTarget(userId, targetId);
        cacheService.set(key, isFavorited, USER_INTERACTION_TTL);
        log.debug("缓存用户收藏关系: userId={}, targetId={}, isFavorited={}", userId, targetId, isFavorited);
    }

    /**
     * 获取用户收藏关系缓存
     */
    public Optional<Boolean> getUserFavoriteTarget(UserId userId, TargetId targetId) {
        String key = CacheKeys.userFavoriteTarget(userId, targetId);
        return cacheService.get(key, Boolean.class);
    }

    // ========================================
    // 用户互动列表缓存
    // ========================================

    /**
     * 缓存用户点赞列表
     */
    public void cacheUserLikes(UserId userId, List<TargetId> likes) {
        String key = CacheKeys.userLikes(userId);
        cacheService.setList(key, likes, USER_LIKES_TTL);
        log.debug("缓存用户点赞列表: userId={}, count={}", userId, likes.size());
    }

    /**
     * 获取用户点赞列表缓存
     */
    public Optional<List<TargetId>> getUserLikes(UserId userId) {
        String key = CacheKeys.userLikes(userId);
        List<TargetId> likes = cacheService.getList(key, TargetId.class);
        return likes.isEmpty() ? Optional.empty() : Optional.of(likes);
    }

    /**
     * 添加点赞到用户点赞列表
     */
    public void addToUserLikes(UserId userId, TargetId targetId) {
        String key = CacheKeys.userLikes(userId);
        cacheService.addToListHead(key, targetId);
        log.debug("添加到用户点赞列表: userId={}, targetId={}", userId, targetId);
    }

    /**
     * 缓存用户收藏列表
     */
    public void cacheUserFavorites(UserId userId, List<TargetId> favorites) {
        String key = CacheKeys.userFavorites(userId);
        cacheService.setList(key, favorites, USER_FAVORITES_TTL);
        log.debug("缓存用户收藏列表: userId={}, count={}", userId, favorites.size());
    }

    /**
     * 获取用户收藏列表缓存
     */
    public Optional<List<TargetId>> getUserFavorites(UserId userId) {
        String key = CacheKeys.userFavorites(userId);
        List<TargetId> favorites = cacheService.getList(key, TargetId.class);
        return favorites.isEmpty() ? Optional.empty() : Optional.of(favorites);
    }

    /**
     * 添加收藏到用户收藏列表
     */
    public void addToUserFavorites(UserId userId, TargetId targetId) {
        String key = CacheKeys.userFavorites(userId);
        cacheService.addToListHead(key, targetId);
        log.debug("添加到用户收藏列表: userId={}, targetId={}", userId, targetId);
    }

    // ========================================
    // 热点互动缓存
    // ========================================

    /**
     * 缓存热门话题标签
     */
    public void cacheHotHashtags(List<String> hotHashtags) {
        String key = CacheKeys.hotHashtags();
        cacheService.setList(key, hotHashtags, Duration.ofHours(1));
        log.debug("缓存热门话题标签: count={}", hotHashtags.size());
    }

    /**
     * 获取热门话题标签缓存
     */
    public Optional<List<String>> getHotHashtags() {
        String key = CacheKeys.hotHashtags();
        List<String> hashtags = cacheService.getList(key, String.class);
        return hashtags.isEmpty() ? Optional.empty() : Optional.of(hashtags);
    }

    /**
     * 更新话题热度
     */
    public void incrementHashtagHeat(String hashtag) {
        String key = "xypai:user:hashtag:heat:" + hashtag;
        long heat = cacheService.increment(key);
        cacheService.expire(key, Duration.ofHours(24)); // 24小时热度统计
        log.debug("更新话题热度: hashtag={}, heat={}", hashtag, heat);
    }

    // ========================================
    // 批量操作
    // ========================================

    /**
     * 批量缓存互动关系
     */
    public void batchCacheUserInteractions(UserId userId, List<TargetId> likedTargets, List<TargetId> favoritedTargets) {
        // 缓存点赞关系
        for (TargetId targetId : likedTargets) {
            cacheUserLikeTarget(userId, targetId, true);
        }

        // 缓存收藏关系
        for (TargetId targetId : favoritedTargets) {
            cacheUserFavoriteTarget(userId, targetId, true);
        }

        log.debug("批量缓存用户互动关系: userId={}, likes={}, favorites={}",
                userId, likedTargets.size(), favoritedTargets.size());
    }

    /**
     * 清除用户所有互动缓存
     */
    public void invalidateUserInteractionCache(UserId userId) {
        // 清除用户点赞列表
        String likesKey = CacheKeys.userLikes(userId);
        cacheService.delete(likesKey);

        // 清除用户收藏列表
        String favoritesKey = CacheKeys.userFavorites(userId);
        cacheService.delete(favoritesKey);

        log.debug("清除用户互动缓存: userId={}", userId);
    }

    /**
     * 预热用户互动缓存
     */
    public void warmupUserInteractionCache(UserId userId, List<TargetId> likes, List<TargetId> favorites) {
        // 并行缓存多个数据
        cacheUserLikes(userId, likes);
        cacheUserFavorites(userId, favorites);

        log.info("预热用户互动缓存完成: userId={}, likes={}, favorites={}",
                userId, likes.size(), favorites.size());
    }

    // ========================================
    // 缓存统计
    // ========================================

    /**
     * 获取互动缓存统计信息
     */
    public InteractionCacheStats getCacheStats() {
        // 这里可以实现缓存命中率等统计信息
        return new InteractionCacheStats(0, 0, 0, 0.0);
    }

    /**
     * 互动缓存统计记录
     */
    public record InteractionCacheStats(
            long interactionStatsHits,
            long userInteractionHits,
            long userListHits,
            double hitRate
    ) {
    }
}
