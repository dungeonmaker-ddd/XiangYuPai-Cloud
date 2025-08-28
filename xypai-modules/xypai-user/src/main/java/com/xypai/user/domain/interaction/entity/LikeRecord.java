package com.xypai.user.domain.interaction.entity;

import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 点赞记录实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class LikeRecord {

    private final UserId userId;        // 点赞用户ID
    private final TargetId targetId;    // 目标对象ID
    private final LocalDateTime likeTime;
    private boolean active;             // 是否活跃（用于软删除）

    private LikeRecord(UserId userId, TargetId targetId, LocalDateTime likeTime, boolean active) {
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.targetId = Objects.requireNonNull(targetId, "目标ID不能为空");
        this.likeTime = Objects.requireNonNull(likeTime, "点赞时间不能为空");
        this.active = active;
    }

    /**
     * 创建点赞记录
     */
    public static LikeRecord create(UserId userId, TargetId targetId) {
        return new LikeRecord(userId, targetId, LocalDateTime.now(), true);
    }

    /**
     * 从数据重建点赞记录
     */
    public static LikeRecord fromData(UserId userId, TargetId targetId,
                                      LocalDateTime likeTime, boolean active) {
        return new LikeRecord(userId, targetId, likeTime, active);
    }

    /**
     * 取消点赞（软删除）
     */
    public void unlike() {
        this.active = false;
    }

    /**
     * 恢复点赞
     */
    public void restore() {
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

    public UserId getUserId() {
        return userId;
    }

    public TargetId getTargetId() {
        return targetId;
    }

    public LocalDateTime getLikeTime() {
        return likeTime;
    }

    // ========================================
    // equals & hashCode
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeRecord that = (LikeRecord) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(targetId, that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, targetId);
    }

    @Override
    public String toString() {
        return String.format("LikeRecord{userId=%s, targetId=%s, likeTime=%s, active=%s}",
                userId, targetId, likeTime, active);
    }
}
