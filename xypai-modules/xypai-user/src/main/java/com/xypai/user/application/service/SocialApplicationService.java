package com.xypai.user.application.service;

import com.xypai.user.application.command.FollowUserCommand;
import com.xypai.user.application.command.UnfollowUserCommand;
import com.xypai.user.application.command.UpdateSocialSettingsCommand;
import com.xypai.user.domain.aggregate.SocialAggregate;
import com.xypai.user.domain.repository.SocialRepository;
import com.xypai.user.domain.repository.UserRepository;
import com.xypai.user.domain.service.DomainEventPublisher;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.infrastructure.cache.SocialGraphCacheService;
import com.xypai.user.insurance.dto.SocialStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 🤝 社交应用服务 - 编排社交业务流程
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialApplicationService {

    private final SocialRepository socialRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;
    private final SocialGraphCacheService socialGraphCacheService;

    /**
     * 👥 关注用户
     */
    @Transactional
    public void followUser(FollowUserCommand command) {
        log.info("用户关注开始: {}", command);

        // 验证目标用户存在
        userRepository.findById(command.followeeId())
                .orElseThrow(() -> new IllegalArgumentException("目标用户不存在: " + command.followeeId()));

        // 获取关注者的社交聚合根
        var followerSocial = socialRepository.findByUserId(command.followerId())
                .orElse(SocialAggregate.create(command.followerId()));

        // 执行关注业务逻辑
        var event = followerSocial.followUser(command.followeeId());

        // 保存关注者的社交聚合根
        socialRepository.save(followerSocial);

        // 获取被关注者的社交聚合根，添加粉丝
        var followeeSocial = socialRepository.findByUserId(command.followeeId())
                .orElse(SocialAggregate.create(command.followeeId()));

        followeeSocial.addFollower(command.followerId());
        socialRepository.save(followeeSocial);

        // 发布领域事件
        eventPublisher.publish(event);
        followerSocial.clearDomainEvents();

        log.info("用户关注完成: {} -> {}", command.followerId(), command.followeeId());
    }

    /**
     * 👥 取消关注用户
     */
    @Transactional
    public void unfollowUser(UnfollowUserCommand command) {
        log.info("取消关注开始: {}", command);

        // 获取关注者的社交聚合根
        var followerSocial = socialRepository.findByUserId(command.followerId())
                .orElseThrow(() -> new IllegalArgumentException("用户社交信息不存在: " + command.followerId()));

        // 执行取消关注业务逻辑
        var event = followerSocial.unfollowUser(command.followeeId());

        // 保存关注者的社交聚合根
        socialRepository.save(followerSocial);

        // 获取被关注者的社交聚合根，移除粉丝
        socialRepository.findByUserId(command.followeeId())
                .ifPresent(followeeSocial -> {
                    followeeSocial.removeFollower(command.followerId());
                    socialRepository.save(followeeSocial);
                });

        // 发布领域事件
        eventPublisher.publish(event);
        followerSocial.clearDomainEvents();

        log.info("取消关注完成: {} -> {}", command.followerId(), command.followeeId());
    }

    /**
     * ⚙️ 更新社交设置
     */
    @Transactional
    public void updateSocialSettings(UpdateSocialSettingsCommand command) {
        log.info("更新社交设置开始: {}", command);

        // 获取用户的社交聚合根
        var socialAggregate = socialRepository.findByUserId(command.userId())
                .orElse(SocialAggregate.create(command.userId()));

        // 更新社交设置
        socialAggregate.updateSettings(command.settings());

        // 保存聚合根
        socialRepository.save(socialAggregate);

        log.info("社交设置更新完成: {}", command.userId());
    }

    /**
     * 🔍 检查关注关系（带缓存）
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(UserId followerId, UserId followeeId) {
        // 先检查缓存
        var cachedResult = socialGraphCacheService.isFollowing(followerId, followeeId);
        if (cachedResult.isPresent()) {
            log.debug("缓存命中 - 关注关系检查: {} -> {}, result={}", followerId, followeeId, cachedResult.get());
            return cachedResult.get();
        }

        // 缓存未命中，查询数据库
        boolean result = socialRepository.findByUserId(followerId)
                .map(social -> social.isFollowing(followeeId))
                .orElse(false);

        // 这里暂时不缓存关注关系，让事件监听器处理

        return result;
    }

    /**
     * 📊 获取关注统计（带缓存）
     */
    @Transactional(readOnly = true)
    public SocialStatsResponse getSocialStats(UserId userId) {
        // 先检查缓存
        var cachedStats = socialGraphCacheService.getSocialStats(userId);
        if (cachedStats.isPresent()) {
            log.debug("缓存命中 - 社交统计: userId={}", userId);
            return cachedStats.get();
        }

        // 缓存未命中，查询数据库
        var socialAggregate = socialRepository.findByUserId(userId)
                .orElse(SocialAggregate.create(userId));

        var stats = SocialStatsResponse.fromAggregate(socialAggregate);

        // 缓存结果
        socialGraphCacheService.cacheSocialStats(userId, stats);

        return stats;
    }


}
