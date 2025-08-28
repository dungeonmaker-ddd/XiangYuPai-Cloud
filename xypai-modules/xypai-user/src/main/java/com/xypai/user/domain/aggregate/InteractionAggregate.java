package com.xypai.user.domain.aggregate;

import com.xypai.user.domain.entity.CommentRecord;
import com.xypai.user.domain.entity.FavoriteRecord;
import com.xypai.user.domain.entity.LikeRecord;
import com.xypai.user.domain.enums.TargetType;
import com.xypai.user.domain.shared.*;
import com.xypai.user.domain.valueobject.InteractionId;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 互动聚合根 - 处理点赞、收藏、评论等互动
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class InteractionAggregate {

    // ========================================
    // 聚合根标识
    // ========================================

    private final InteractionId interactionId;
    private final TargetId targetId;              // 目标对象ID
    private final TargetType targetType;          // 目标类型

    // ========================================
    // 互动记录
    // ========================================

    private final List<LikeRecord> likes;         // 点赞记录
    private final List<FavoriteRecord> favorites; // 收藏记录  
    private final List<CommentRecord> comments;   // 评论记录

    // ========================================
    // 领域事件
    // ========================================

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ========================================
    // 构造器
    // ========================================

    private InteractionAggregate(InteractionId interactionId, TargetId targetId, TargetType targetType) {
        this.interactionId = Objects.requireNonNull(interactionId, "互动ID不能为空");
        this.targetId = Objects.requireNonNull(targetId, "目标ID不能为空");
        this.targetType = Objects.requireNonNull(targetType, "目标类型不能为空");
        this.likes = new ArrayList<>();
        this.favorites = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    private InteractionAggregate(
            InteractionId interactionId,
            TargetId targetId,
            TargetType targetType,
            List<LikeRecord> likes,
            List<FavoriteRecord> favorites,
            List<CommentRecord> comments
    ) {
        this.interactionId = Objects.requireNonNull(interactionId, "互动ID不能为空");
        this.targetId = Objects.requireNonNull(targetId, "目标ID不能为空");
        this.targetType = Objects.requireNonNull(targetType, "目标类型不能为空");
        this.likes = new ArrayList<>(likes != null ? likes : new ArrayList<>());
        this.favorites = new ArrayList<>(favorites != null ? favorites : new ArrayList<>());
        this.comments = new ArrayList<>(comments != null ? comments : new ArrayList<>());
    }

    // ========================================
    // 工厂方法
    // ========================================

    /**
     * 创建新的互动聚合根
     */
    public static InteractionAggregate create(TargetId targetId, TargetType targetType) {
        var interactionId = InteractionId.generate();
        return new InteractionAggregate(interactionId, targetId, targetType);
    }

    /**
     * 从现有数据重建聚合根
     */
    public static InteractionAggregate fromExisting(
            InteractionId interactionId,
            TargetId targetId,
            TargetType targetType,
            List<LikeRecord> likes,
            List<FavoriteRecord> favorites,
            List<CommentRecord> comments
    ) {
        return new InteractionAggregate(interactionId, targetId, targetType, likes, favorites, comments);
    }

    // ========================================
    // 业务方法 - 点赞功能
    // ========================================

    /**
     * 点赞目标
     */
    public DomainEvent likeTarget(UserId userId) {
        // 验证业务规则
        validateNotAlreadyLiked(userId);

        // 执行业务逻辑
        var likeRecord = LikeRecord.create(userId, targetId);
        likes.add(likeRecord);

        // 返回领域事件
        var event = TargetLikedEvent.create(targetId, targetType, userId);
        addDomainEvent(event);
        return event;
    }

    /**
     * 取消点赞
     */
    public DomainEvent unlikeTarget(UserId userId) {
        // 查找并移除点赞记录
        boolean removed = likes.removeIf(like ->
                like.getUserId().equals(userId) && like.isActive());

        if (!removed) {
            throw new IllegalArgumentException("未点赞该目标，无法取消点赞");
        }

        // 返回领域事件
        var event = TargetUnlikedEvent.create(targetId, targetType, userId);
        addDomainEvent(event);
        return event;
    }

    // ========================================
    // 业务方法 - 收藏功能
    // ========================================

    /**
     * 收藏目标
     */
    public DomainEvent favoriteTarget(UserId userId) {
        // 验证业务规则
        validateNotAlreadyFavorited(userId);

        // 执行业务逻辑
        var favoriteRecord = FavoriteRecord.create(userId, targetId);
        favorites.add(favoriteRecord);

        // 返回领域事件
        var event = TargetFavoritedEvent.create(targetId, targetType, userId);
        addDomainEvent(event);
        return event;
    }

    /**
     * 取消收藏
     */
    public DomainEvent unfavoriteTarget(UserId userId) {
        // 查找并移除收藏记录
        boolean removed = favorites.removeIf(favorite ->
                favorite.getUserId().equals(userId) && favorite.isActive());

        if (!removed) {
            throw new IllegalArgumentException("未收藏该目标，无法取消收藏");
        }

        // 返回领域事件
        var event = TargetUnfavoritedEvent.create(targetId, targetType, userId);
        addDomainEvent(event);
        return event;
    }

    // ========================================
    // 业务方法 - 评论功能
    // ========================================

    /**
     * 添加评论
     */
    public String addComment(UserId userId, String content) {
        // 验证业务规则
        validateCommentContent(content);

        // 执行业务逻辑
        var commentRecord = CommentRecord.create(userId, targetId, content);
        comments.add(commentRecord);

        // TODO: 发布评论事件
        return commentRecord.getCommentId();
    }

    /**
     * 删除评论
     */
    public void deleteComment(String commentId, UserId userId) {
        var comment = comments.stream()
                .filter(c -> c.getCommentId().equals(commentId) && c.isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        // 验证权限（只能删除自己的评论）
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能删除自己的评论");
        }

        comment.delete();
        // TODO: 发布评论删除事件
    }

    // ========================================
    // 查询方法
    // ========================================

    /**
     * 检查用户是否点赞了目标
     */
    public boolean isLikedBy(UserId userId) {
        return likes.stream()
                .anyMatch(like -> like.getUserId().equals(userId) && like.isActive());
    }

    /**
     * 检查用户是否收藏了目标
     */
    public boolean isFavoritedBy(UserId userId) {
        return favorites.stream()
                .anyMatch(favorite -> favorite.getUserId().equals(userId) && favorite.isActive());
    }

    /**
     * 获取点赞数
     */
    public int getLikeCount() {
        return (int) likes.stream()
                .filter(LikeRecord::isActive)
                .count();
    }

    /**
     * 获取收藏数
     */
    public int getFavoriteCount() {
        return (int) favorites.stream()
                .filter(FavoriteRecord::isActive)
                .count();
    }

    /**
     * 获取评论数
     */
    public int getCommentCount() {
        return (int) comments.stream()
                .filter(CommentRecord::isActive)
                .count();
    }

    /**
     * 获取活跃的点赞列表
     */
    public List<LikeRecord> getActiveLikes() {
        return likes.stream()
                .filter(LikeRecord::isActive)
                .collect(Collectors.toList());
    }

    /**
     * 获取活跃的收藏列表
     */
    public List<FavoriteRecord> getActiveFavorites() {
        return favorites.stream()
                .filter(FavoriteRecord::isActive)
                .collect(Collectors.toList());
    }

    /**
     * 获取活跃的评论列表
     */
    public List<CommentRecord> getActiveComments() {
        return comments.stream()
                .filter(CommentRecord::isActive)
                .collect(Collectors.toList());
    }

    // ========================================
    // 业务规则验证
    // ========================================

    private void validateNotAlreadyLiked(UserId userId) {
        if (isLikedBy(userId)) {
            throw new IllegalArgumentException("已经点赞了该目标");
        }
    }

    private void validateNotAlreadyFavorited(UserId userId) {
        if (isFavoritedBy(userId)) {
            throw new IllegalArgumentException("已经收藏了该目标");
        }
    }

    private void validateCommentContent(String content) {
        Objects.requireNonNull(content, "评论内容不能为空");
        if (content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("评论内容不能超过500字符");
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

    public InteractionId getInteractionId() {
        return interactionId;
    }

    public TargetId getTargetId() {
        return targetId;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public List<LikeRecord> getLikes() {
        return List.copyOf(likes);
    }

    public List<FavoriteRecord> getFavorites() {
        return List.copyOf(favorites);
    }

    public List<CommentRecord> getComments() {
        return List.copyOf(comments);
    }
}
