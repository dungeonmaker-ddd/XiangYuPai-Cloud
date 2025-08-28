package com.xypai.user.domain.entity;

import com.xypai.user.domain.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 👥 关注关系实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class FollowRelation {

    private final UserId followerId;    // 关注者ID
    private final UserId followeeId;    // 被关注者ID
    private final LocalDateTime followTime;
    private boolean active;             // 是否活跃（用于软删除）

    private FollowRelation(UserId followerId, UserId followeeId, LocalDateTime followTime, boolean active) {
        this.followerId = Objects.requireNonNull(followerId, "关注者ID不能为空");
        this.followeeId = Objects.requireNonNull(followeeId, "被关注者ID不能为空");
        this.followTime = Objects.requireNonNull(followTime, "关注时间不能为空");
        this.active = active;

        // 业务规则验证
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("不能关注自己");
        }
    }

    /**
     * 创建关注关系
     */
    public static FollowRelation create(UserId followerId, UserId followeeId) {
        return new FollowRelation(followerId, followeeId, LocalDateTime.now(), true);
    }

    /**
     * 从数据重建关注关系
     */
    public static FollowRelation fromData(UserId followerId, UserId followeeId, LocalDateTime followTime, boolean active) {
        return new FollowRelation(followerId, followeeId, followTime, active);
    }

    /**
     * 取消关注（软删除）
     */
    public void unfollow() {
        this.active = false;
    }

    /**
     * 恢复关注
     */
    public void refollow() {
        this.active = true;
    }

    /**
     * 检查是否活跃
     */
    public boolean isActive() {
        return active;
    }

    // ========================================
    // Getters
    // ========================================

    public UserId getFollowerId() {
        return followerId;
    }

    public UserId getFolloweeId() {
        return followeeId;
    }

    public LocalDateTime getFollowTime() {
        return followTime;
    }

    // ========================================
    // equals & hashCode
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowRelation that = (FollowRelation) o;
        return Objects.equals(followerId, that.followerId) &&
                Objects.equals(followeeId, that.followeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followerId, followeeId);
    }

    @Override
    public String toString() {
        return String.format("FollowRelation{followerId=%s, followeeId=%s, followTime=%s, active=%s}",
                followerId, followeeId, followTime, active);
    }
}
