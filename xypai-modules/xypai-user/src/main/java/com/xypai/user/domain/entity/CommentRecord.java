package com.xypai.user.domain.entity;

import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 评论记录实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class CommentRecord {

    private final String commentId;     // 评论ID
    private final UserId userId;        // 评论用户ID
    private final TargetId targetId;    // 目标对象ID
    private final String content;       // 评论内容
    private final LocalDateTime commentTime;
    private boolean active;             // 是否活跃（用于软删除）

    private CommentRecord(String commentId, UserId userId, TargetId targetId,
                          String content, LocalDateTime commentTime, boolean active) {
        this.commentId = Objects.requireNonNull(commentId, "评论ID不能为空");
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.targetId = Objects.requireNonNull(targetId, "目标ID不能为空");
        this.content = validateContent(content);
        this.commentTime = Objects.requireNonNull(commentTime, "评论时间不能为空");
        this.active = active;
    }

    /**
     * 创建评论记录
     */
    public static CommentRecord create(UserId userId, TargetId targetId, String content) {
        String commentId = java.util.UUID.randomUUID().toString();
        return new CommentRecord(commentId, userId, targetId, content, LocalDateTime.now(), true);
    }

    /**
     * 从数据重建评论记录
     */
    public static CommentRecord fromData(String commentId, UserId userId, TargetId targetId,
                                         String content, LocalDateTime commentTime, boolean active) {
        return new CommentRecord(commentId, userId, targetId, content, commentTime, active);
    }

    /**
     * 验证评论内容
     */
    private static String validateContent(String content) {
        Objects.requireNonNull(content, "评论内容不能为空");
        if (content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("评论内容不能超过500字符");
        }
        return content.trim();
    }

    /**
     * 删除评论（软删除）
     */
    public void delete() {
        this.active = false;
    }

    /**
     * 恢复评论
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

    public String getCommentId() {
        return commentId;
    }

    public UserId getUserId() {
        return userId;
    }

    public TargetId getTargetId() {
        return targetId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCommentTime() {
        return commentTime;
    }

    // ========================================
    // equals & hashCode
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentRecord that = (CommentRecord) o;
        return Objects.equals(commentId, that.commentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId);
    }

    @Override
    public String toString() {
        return String.format("CommentRecord{commentId=%s, userId=%s, targetId=%s, content='%s', commentTime=%s, active=%s}",
                commentId, userId, targetId, content, commentTime, active);
    }
}
