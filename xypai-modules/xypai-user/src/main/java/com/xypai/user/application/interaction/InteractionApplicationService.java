package com.xypai.user.application.interaction;

import com.xypai.user.application.interaction.command.*;
import com.xypai.user.domain.interaction.InteractionAggregate;
import com.xypai.user.domain.interaction.repository.InteractionRepository;
import com.xypai.user.domain.interaction.valueobject.TargetId;
import com.xypai.user.domain.shared.service.DomainEventPublisher;
import com.xypai.user.domain.user.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 互动应用服务 - 编排互动业务流程
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionApplicationService {

    private final InteractionRepository interactionRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * 点赞目标
     */
    @Transactional
    public void likeTarget(LikeTargetCommand command) {
        log.info("点赞目标开始: {}", command);

        // 获取或创建互动聚合根
        var interactionAggregate = interactionRepository
                .findByTargetIdAndType(command.targetId(), command.targetType())
                .orElse(InteractionAggregate.create(command.targetId(), command.targetType()));

        // 执行点赞业务逻辑
        var event = interactionAggregate.likeTarget(command.userId());

        // 保存聚合根
        interactionRepository.save(interactionAggregate);

        // 发布领域事件
        eventPublisher.publish(event);
        interactionAggregate.clearDomainEvents();

        log.info("点赞目标完成: {} -> {}", command.userId(), command.targetId());
    }

    /**
     * 取消点赞目标
     */
    @Transactional
    public void unlikeTarget(UnlikeTargetCommand command) {
        log.info("取消点赞目标开始: {}", command);

        // 获取互动聚合根
        var interactionAggregate = interactionRepository
                .findByTargetIdAndType(command.targetId(), command.targetType())
                .orElseThrow(() -> new IllegalArgumentException("目标互动信息不存在"));

        // 执行取消点赞业务逻辑
        var event = interactionAggregate.unlikeTarget(command.userId());

        // 保存聚合根
        interactionRepository.save(interactionAggregate);

        // 发布领域事件
        eventPublisher.publish(event);
        interactionAggregate.clearDomainEvents();

        log.info("取消点赞目标完成: {} -> {}", command.userId(), command.targetId());
    }

    /**
     * 收藏目标
     */
    @Transactional
    public void favoriteTarget(FavoriteTargetCommand command) {
        log.info("收藏目标开始: {}", command);

        // 获取或创建互动聚合根
        var interactionAggregate = interactionRepository
                .findByTargetIdAndType(command.targetId(), command.targetType())
                .orElse(InteractionAggregate.create(command.targetId(), command.targetType()));

        // 执行收藏业务逻辑
        var event = interactionAggregate.favoriteTarget(command.userId());

        // 保存聚合根
        interactionRepository.save(interactionAggregate);

        // 发布领域事件
        eventPublisher.publish(event);
        interactionAggregate.clearDomainEvents();

        log.info("收藏目标完成: {} -> {}", command.userId(), command.targetId());
    }

    /**
     * 取消收藏目标
     */
    @Transactional
    public void unfavoriteTarget(UnfavoriteTargetCommand command) {
        log.info("取消收藏目标开始: {}", command);

        // 获取互动聚合根
        var interactionAggregate = interactionRepository
                .findByTargetIdAndType(command.targetId(), command.targetType())
                .orElseThrow(() -> new IllegalArgumentException("目标互动信息不存在"));

        // 执行取消收藏业务逻辑
        var event = interactionAggregate.unfavoriteTarget(command.userId());

        // 保存聚合根
        interactionRepository.save(interactionAggregate);

        // 发布领域事件
        eventPublisher.publish(event);
        interactionAggregate.clearDomainEvents();

        log.info("取消收藏目标完成: {} -> {}", command.userId(), command.targetId());
    }

    /**
     * 添加评论
     */
    @Transactional
    public String addComment(AddCommentCommand command) {
        log.info("添加评论开始: {}", command);

        // 获取或创建互动聚合根
        var interactionAggregate = interactionRepository
                .findByTargetIdAndType(command.targetId(), command.targetType())
                .orElse(InteractionAggregate.create(command.targetId(), command.targetType()));

        // 执行添加评论业务逻辑
        var commentId = interactionAggregate.addComment(command.userId(), command.content());

        // 保存聚合根
        interactionRepository.save(interactionAggregate);

        // 发布领域事件
        eventPublisher.publishAll(interactionAggregate.getDomainEvents());
        interactionAggregate.clearDomainEvents();

        log.info("添加评论完成: {} -> {}, commentId={}", command.userId(), command.targetId(), commentId);
        return commentId;
    }

    /**
     * 检查互动状态
     */
    @Transactional(readOnly = true)
    public InteractionStatus getInteractionStatus(TargetId targetId, UserId userId) {
        var interactionAggregate = interactionRepository.findByTargetId(targetId);

        if (interactionAggregate.isEmpty()) {
            return new InteractionStatus(false, false, 0, 0, 0);
        }

        var aggregate = interactionAggregate.get();
        return new InteractionStatus(
                aggregate.isLikedBy(userId),
                aggregate.isFavoritedBy(userId),
                aggregate.getLikeCount(),
                aggregate.getFavoriteCount(),
                aggregate.getCommentCount()
        );
    }

    /**
     * 互动状态记录
     */
    public record InteractionStatus(
            boolean isLiked,
            boolean isFavorited,
            int likeCount,
            int favoriteCount,
            int commentCount
    ) {
    }
}
