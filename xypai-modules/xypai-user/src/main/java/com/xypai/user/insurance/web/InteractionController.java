package com.xypai.user.insurance.web;

import com.xypai.user.application.command.*;
import com.xypai.user.application.service.InteractionApplicationService;
import com.xypai.user.domain.enums.TargetType;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.insurance.dto.InteractionStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 互动控制器 - 接口层
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@RestController
@RequestMapping("/api/interactions")
@Tag(name = "互动管理", description = "点赞、收藏、评论等互动功能接口")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionApplicationService interactionApplicationService;

    /**
     * 点赞目标
     */
    @Operation(summary = "点赞目标", description = "对指定目标进行点赞")
    @PostMapping("/{targetType}/{targetId}/like")
    public ResponseEntity<Void> likeTarget(
            @Parameter(description = "目标类型") @PathVariable String targetType,
            @Parameter(description = "目标ID") @PathVariable String targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {

        log.info("点赞目标请求: userId={}, targetType={}, targetId={}", userId, targetType, targetId);

        var command = new LikeTargetCommand(
                UserId.of(userId),
                TargetId.of(targetId),
                TargetType.fromCode(targetType)
        );

        interactionApplicationService.likeTarget(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 取消点赞目标
     */
    @Operation(summary = "取消点赞目标", description = "取消对指定目标的点赞")
    @DeleteMapping("/{targetType}/{targetId}/like")
    public ResponseEntity<Void> unlikeTarget(
            @Parameter(description = "目标类型") @PathVariable String targetType,
            @Parameter(description = "目标ID") @PathVariable String targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {

        log.info("取消点赞目标请求: userId={}, targetType={}, targetId={}", userId, targetType, targetId);

        var command = new UnlikeTargetCommand(
                UserId.of(userId),
                TargetId.of(targetId),
                TargetType.fromCode(targetType)
        );

        interactionApplicationService.unlikeTarget(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 收藏目标
     */
    @Operation(summary = "收藏目标", description = "收藏指定目标")
    @PostMapping("/{targetType}/{targetId}/favorite")
    public ResponseEntity<Void> favoriteTarget(
            @Parameter(description = "目标类型") @PathVariable String targetType,
            @Parameter(description = "目标ID") @PathVariable String targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {

        log.info("收藏目标请求: userId={}, targetType={}, targetId={}", userId, targetType, targetId);

        var command = new FavoriteTargetCommand(
                UserId.of(userId),
                TargetId.of(targetId),
                TargetType.fromCode(targetType)
        );

        interactionApplicationService.favoriteTarget(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 取消收藏目标
     */
    @Operation(summary = "取消收藏目标", description = "取消收藏指定目标")
    @DeleteMapping("/{targetType}/{targetId}/favorite")
    public ResponseEntity<Void> unfavoriteTarget(
            @Parameter(description = "目标类型") @PathVariable String targetType,
            @Parameter(description = "目标ID") @PathVariable String targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {

        log.info("取消收藏目标请求: userId={}, targetType={}, targetId={}", userId, targetType, targetId);

        var command = new UnfavoriteTargetCommand(
                UserId.of(userId),
                TargetId.of(targetId),
                TargetType.fromCode(targetType)
        );

        interactionApplicationService.unfavoriteTarget(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 添加评论
     */
    @Operation(summary = "添加评论", description = "对指定目标添加评论")
    @PostMapping("/{targetType}/{targetId}/comment")
    public ResponseEntity<String> addComment(
            @Parameter(description = "目标类型") @PathVariable String targetType,
            @Parameter(description = "目标ID") @PathVariable String targetId,
            @Valid @RequestBody AddCommentRequest request) {

        log.info("添加评论请求: userId={}, targetType={}, targetId={}, content={}",
                request.userId(), targetType, targetId, request.content());

        var command = new AddCommentCommand(
                UserId.of(request.userId()),
                TargetId.of(targetId),
                TargetType.fromCode(targetType),
                request.content()
        );

        var commentId = interactionApplicationService.addComment(command);
        return ResponseEntity.ok(commentId);
    }

    /**
     * 获取互动状态
     */
    @Operation(summary = "获取互动状态", description = "获取目标的互动状态（点赞数、收藏数等）")
    @GetMapping("/{targetType}/{targetId}/status")
    public ResponseEntity<InteractionStatusResponse> getInteractionStatus(
            @Parameter(description = "目标类型") @PathVariable String targetType,
            @Parameter(description = "目标ID") @PathVariable String targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {

        var status = interactionApplicationService.getInteractionStatus(
                TargetId.of(targetId),
                UserId.of(userId)
        );

        var response = new InteractionStatusResponse(
                status.isLiked(),
                status.isFavorited(),
                status.likeCount(),
                status.favoriteCount(),
                status.commentCount()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 添加评论请求DTO
     */
    public record AddCommentRequest(
            Long userId,
            String content
    ) {
    }
}
