package com.xypai.user.infrastructure.cache;

import com.xypai.user.domain.service.CacheService;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.insurance.dto.SocialStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * 社交图缓存服务 - 专门处理社交关系的缓存
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialGraphCacheService {

    // 缓存过期时间配置
    private static final Duration FOLLOWING_TTL = Duration.ofHours(6);      // 关注列表缓存6小时
    private static final Duration FOLLOWERS_TTL = Duration.ofHours(4);      // 粉丝列表缓存4小时
    private static final Duration SOCIAL_STATS_TTL = Duration.ofMinutes(30); // 社交统计缓存30分钟
    private static final Duration FOLLOW_RELATION_TTL = Duration.ofMinutes(15); // 关注关系缓存15分钟
    private final CacheService cacheService;

    // ========================================
    // 关注关系缓存
    // ========================================

    /**
     * 缓存用户关注列表
     */
    public void cacheUserFollowings(UserId userId, Set<UserId> followings) {
        String key = CacheKeys.userFollowings(userId);
        cacheService.setSet(key, followings, FOLLOWING_TTL);
        log.debug("缓存用户关注列表: userId={}, count={}", userId, followings.size());
    }

    /**
     * 获取用户关注列表缓存
     */
    public Optional<Set<UserId>> getUserFollowings(UserId userId) {
        String key = CacheKeys.userFollowings(userId);
        Set<UserId> followings = cacheService.getSet(key, UserId.class);
        return followings.isEmpty() ? Optional.empty() : Optional.of(followings);
    }

    /**
     * 添加关注关系到缓存
     */
    public void addFollowing(UserId followerId, UserId followeeId) {
        // 更新关注列表缓存
        String followingKey = CacheKeys.userFollowings(followerId);
        cacheService.addToSet(followingKey, followeeId);

        // 更新粉丝列表缓存
        String followerKey = CacheKeys.userFollowers(followeeId);
        cacheService.addToSet(followerKey, followerId);

        // 缓存关注关系
        String relationKey = CacheKeys.followRelation(followerId, followeeId);
        cacheService.set(relationKey, true, FOLLOW_RELATION_TTL);

        // 清除社交统计缓存
        invalidateSocialStats(followerId);
        invalidateSocialStats(followeeId);

        log.debug("添加关注关系缓存: {} -> {}", followerId, followeeId);
    }

    /**
     * 移除关注关系缓存
     */
    public void removeFollowing(UserId followerId, UserId followeeId) {
        // 更新关注列表缓存
        String followingKey = CacheKeys.userFollowings(followerId);
        cacheService.removeFromSet(followingKey, followeeId);

        // 更新粉丝列表缓存
        String followerKey = CacheKeys.userFollowers(followeeId);
        cacheService.removeFromSet(followerKey, followerId);

        // 删除关注关系缓存
        String relationKey = CacheKeys.followRelation(followerId, followeeId);
        cacheService.delete(relationKey);

        // 清除社交统计缓存
        invalidateSocialStats(followerId);
        invalidateSocialStats(followeeId);

        log.debug("移除关注关系缓存: {} -> {}", followerId, followeeId);
    }

    /**
     * 检查关注关系缓存
     */
    public Optional<Boolean> isFollowing(UserId followerId, UserId followeeId) {
        String relationKey = CacheKeys.followRelation(followerId, followeeId);
        return cacheService.get(relationKey, Boolean.class);
    }

    // ========================================
    // 粉丝关系缓存
    // ========================================

    /**
     * 缓存用户粉丝列表
     */
    public void cacheUserFollowers(UserId userId, Set<UserId> followers) {
        String key = CacheKeys.userFollowers(userId);
        cacheService.setSet(key, followers, FOLLOWERS_TTL);
        log.debug("缓存用户粉丝列表: userId={}, count={}", userId, followers.size());
    }

    /**
     * 获取用户粉丝列表缓存
     */
    public Optional<Set<UserId>> getUserFollowers(UserId userId) {
        String key = CacheKeys.userFollowers(userId);
        Set<UserId> followers = cacheService.getSet(key, UserId.class);
        return followers.isEmpty() ? Optional.empty() : Optional.of(followers);
    }

    // ========================================
    // 社交统计缓存
    // ========================================

    /**
     * 缓存社交统计信息
     */
    public void cacheSocialStats(UserId userId, SocialStatsResponse stats) {
        String key = CacheKeys.socialStats(userId);
        cacheService.set(key, stats, SOCIAL_STATS_TTL);
        log.debug("缓存社交统计: userId={}, following={}, followers={}",
                userId, stats.followingCount(), stats.followerCount());
    }

    /**
     * 获取社交统计缓存
     */
    public Optional<SocialStatsResponse> getSocialStats(UserId userId) {
        String key = CacheKeys.socialStats(userId);
        return cacheService.get(key, SocialStatsResponse.class);
    }

    /**
     * 清除社交统计缓存
     */
    public void invalidateSocialStats(UserId userId) {
        String key = CacheKeys.socialStats(userId);
        cacheService.delete(key);
        log.debug("清除社交统计缓存: userId={}", userId);
    }

    // ========================================
    // 批量操作
    // ========================================

    /**
     * 清除用户所有社交相关缓存
     */
    public void invalidateUserSocialCache(UserId userId) {
        String pattern = CacheKeys.socialPattern(userId);
        cacheService.deletePattern(pattern);
        log.debug("清除用户社交缓存: userId={}", userId);
    }

    /**
     * 预热用户社交缓存
     */
    public void warmupUserSocialCache(UserId userId, Set<UserId> followings,
                                      Set<UserId> followers, SocialStatsResponse stats) {
        // 并行缓存多个数据
        cacheUserFollowings(userId, followings);
        cacheUserFollowers(userId, followers);
        cacheSocialStats(userId, stats);

        log.info("预热用户社交缓存完成: userId={}", userId);
    }

    // ========================================
    // 热点用户缓存
    // ========================================

    /**
     * 缓存热门用户列表
     */
    public void cacheHotUsers(Set<UserId> hotUsers) {
        String key = CacheKeys.hotUsers();
        cacheService.setSet(key, hotUsers, Duration.ofHours(1));
        log.debug("缓存热门用户列表: count={}", hotUsers.size());
    }

    /**
     * 获取热门用户列表缓存
     */
    public Optional<Set<UserId>> getHotUsers() {
        String key = CacheKeys.hotUsers();
        Set<UserId> hotUsers = cacheService.getSet(key, UserId.class);
        return hotUsers.isEmpty() ? Optional.empty() : Optional.of(hotUsers);
    }

    /**
     * 添加用户到热门列表
     */
    public void addToHotUsers(UserId userId) {
        String key = CacheKeys.hotUsers();
        cacheService.addToSet(key, userId);
        log.debug("添加用户到热门列表: userId={}", userId);
    }

    // ========================================
    // 缓存统计
    // ========================================

    /**
     * 获取社交缓存统计信息
     */
    public SocialCacheStats getCacheStats() {
        // 这里可以实现缓存命中率等统计信息
        return new SocialCacheStats(0, 0, 0.0);
    }

    /**
     * 社交缓存统计记录
     */
    public record SocialCacheStats(
            long followingCacheHits,
            long followerCacheHits,
            double hitRate
    ) {
    }
}
