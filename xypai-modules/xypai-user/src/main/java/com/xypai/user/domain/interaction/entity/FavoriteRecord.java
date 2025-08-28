package com.xypai.user.domain.interaction.entity;

import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 收藏记录实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class FavoriteRecord {

    private final UserId userId;        // 收藏用户ID
    private final TargetId targetId;    // 目标对象ID
    private final LocalDateTime favoriteTime;
    private boolean active;             // 是否活跃（用于软删除）

    private FavoriteRecord(UserId userId, TargetId targetId, LocalDateTime favoriteTime, boolean active) {
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.targetId = Objects.requireNonNull(targetId, "目标ID不能为空");
        this.favoriteTime = Objects.requireNonNull(favoriteTime, "收藏时间不能为空");
        this.active = active;
    }

    /**
     * 创建收藏记录
     */
    public static FavoriteRecord create(UserId userId, TargetId targetId) {
        return new FavoriteRecord(userId, targetId, LocalDateTime.now(), true);
    }

    /**
     * 从数据重建收藏记录
     */
    public static FavoriteRecord fromData(UserId userId, TargetId targetId,
                                          LocalDateTime favoriteTime, boolean active) {
        return new FavoriteRecord(userId, targetId, favoriteTime, active);
    }

    /**
     * 取消收藏（软删除）
     */
    public void unfavorite() {
        this.active = false;
    }

    /**
     * 恢复收藏
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

    public LocalDateTime getFavoriteTime() {
        return favoriteTime;
    }

    // ========================================
    // equals & hashCode
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteRecord that = (FavoriteRecord) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(targetId, that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, targetId);
    }

    @Override
    public String toString() {
        return String.format("FavoriteRecord{userId=%s, targetId=%s, favoriteTime=%s, active=%s}",
                userId, targetId, favoriteTime, active);
    }
}
