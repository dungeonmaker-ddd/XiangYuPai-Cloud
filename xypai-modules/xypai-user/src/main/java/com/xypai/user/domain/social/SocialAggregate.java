package com.xypai.user.domain.social;

import com.xypai.user.domain.shared.DomainEvent;
import com.xypai.user.domain.shared.UserFollowedEvent;
import com.xypai.user.domain.shared.UserUnfollowedEvent;
import com.xypai.user.domain.social.entity.FollowRelation;
import com.xypai.user.domain.social.entity.SocialSettings;
import com.xypai.user.domain.social.valueobject.SocialId;
import com.xypai.user.domain.user.valueobject.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 🤝 社交聚合根 - 处理用户社交关系
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class SocialAggregate {

    // ========================================
    // 聚合根标识
    // ========================================

    private final SocialId socialId;
    private final UserId userId;

    // ========================================
    // 社交关系
    // ========================================

    private final List<FollowRelation> followings;    // 我关注的人
    private final List<FollowRelation> followers;     // 关注我的人
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ========================================
    // 领域事件
    // ========================================
    private SocialSettings settings;                  // 社交设置

    // ========================================
    // 构造器
    // ========================================

    private SocialAggregate(SocialId socialId, UserId userId, SocialSettings settings) {
        this.socialId = Objects.requireNonNull(socialId, "社交ID不能为空");
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.settings = Objects.requireNonNull(settings, "社交设置不能为空");
        this.followings = new ArrayList<>();
        this.followers = new ArrayList<>();
    }

    private SocialAggregate(
            SocialId socialId,
            UserId userId,
            List<FollowRelation> followings,
            List<FollowRelation> followers,
            SocialSettings settings
    ) {
        this.socialId = Objects.requireNonNull(socialId, "社交ID不能为空");
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.followings = new ArrayList<>(followings != null ? followings : new ArrayList<>());
        this.followers = new ArrayList<>(followers != null ? followers : new ArrayList<>());
        this.settings = Objects.requireNonNull(settings, "社交设置不能为空");
    }

    // ========================================
    // 工厂方法
    // ========================================

    /**
     * 🔨 创建新的社交聚合根
     */
    public static SocialAggregate create(UserId userId) {
        var socialId = SocialId.generate();
        var settings = SocialSettings.createDefault();
        return new SocialAggregate(socialId, userId, settings);
    }

    /**
     * 🔄 从现有数据重建聚合根
     */
    public static SocialAggregate fromExisting(
            SocialId socialId,
            UserId userId,
            List<FollowRelation> followings,
            List<FollowRelation> followers,
            SocialSettings settings
    ) {
        return new SocialAggregate(socialId, userId, followings, followers, settings);
    }

    // ========================================
    // 业务方法
    // ========================================

    /**
     * 🎯 关注用户
     */
    public DomainEvent followUser(UserId targetUserId) {
        // 验证业务规则
        validateNotSelf(targetUserId);
        validateNotAlreadyFollowing(targetUserId);

        // 执行业务逻辑
        var followRelation = FollowRelation.create(userId, targetUserId);
        followings.add(followRelation);

        // 返回领域事件
        var event = UserFollowedEvent.create(userId, targetUserId);
        addDomainEvent(event);
        return event;
    }

    /**
     * 🎯 取消关注用户
     */
    public DomainEvent unfollowUser(UserId targetUserId) {
        // 验证业务规则
        validateNotSelf(targetUserId);

        // 查找并移除关注关系
        boolean removed = followings.removeIf(relation ->
                relation.getFolloweeId().equals(targetUserId) && relation.isActive());

        if (!removed) {
            throw new IllegalArgumentException("未关注该用户，无法取消关注");
        }

        // 返回领域事件
        var event = UserUnfollowedEvent.create(userId, targetUserId);
        addDomainEvent(event);
        return event;
    }

    /**
     * 🎯 添加粉丝（被其他用户关注时调用）
     */
    public void addFollower(UserId followerId) {
        // 验证不是自己
        validateNotSelf(followerId);

        // 检查是否已经是粉丝
        boolean alreadyFollower = followers.stream()
                .anyMatch(relation -> relation.getFollowerId().equals(followerId) && relation.isActive());

        if (!alreadyFollower) {
            var followerRelation = FollowRelation.create(followerId, userId);
            followers.add(followerRelation);
        }
    }

    /**
     * 🎯 移除粉丝（被其他用户取消关注时调用）
     */
    public void removeFollower(UserId followerId) {
        followers.removeIf(relation ->
                relation.getFollowerId().equals(followerId));
    }

    /**
     * ⚙️ 更新社交设置
     */
    public void updateSettings(SocialSettings newSettings) {
        this.settings = Objects.requireNonNull(newSettings, "社交设置不能为空");
    }

    // ========================================
    // 查询方法
    // ========================================

    /**
     * 检查是否关注了某用户
     */
    public boolean isFollowing(UserId targetUserId) {
        return followings.stream()
                .anyMatch(relation -> relation.getFolloweeId().equals(targetUserId) && relation.isActive());
    }

    /**
     * 检查某用户是否关注了我
     */
    public boolean isFollowedBy(UserId followerId) {
        return followers.stream()
                .anyMatch(relation -> relation.getFollowerId().equals(followerId) && relation.isActive());
    }

    /**
     * 获取关注数
     */
    public int getFollowingCount() {
        return (int) followings.stream()
                .filter(FollowRelation::isActive)
                .count();
    }

    /**
     * 获取粉丝数
     */
    public int getFollowerCount() {
        return (int) followers.stream()
                .filter(FollowRelation::isActive)
                .count();
    }

    /**
     * 获取活跃的关注列表
     */
    public List<FollowRelation> getActiveFollowings() {
        return followings.stream()
                .filter(FollowRelation::isActive)
                .collect(Collectors.toList());
    }

    /**
     * 获取活跃的粉丝列表
     */
    public List<FollowRelation> getActiveFollowers() {
        return followers.stream()
                .filter(FollowRelation::isActive)
                .collect(Collectors.toList());
    }

    // ========================================
    // 业务规则验证
    // ========================================

    private void validateNotSelf(UserId targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("不能对自己执行此操作");
        }
    }

    private void validateNotAlreadyFollowing(UserId targetUserId) {
        if (isFollowing(targetUserId)) {
            throw new IllegalArgumentException("已经关注了该用户");
        }
    }

    // ========================================
    // 领域事件管理
    // ========================================

    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========================================
    // Getters
    // ========================================

    public SocialId getSocialId() {
        return socialId;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<FollowRelation> getFollowings() {
        return List.copyOf(followings);
    }

    public List<FollowRelation> getFollowers() {
        return List.copyOf(followers);
    }

    public SocialSettings getSettings() {
        return settings;
    }
}
