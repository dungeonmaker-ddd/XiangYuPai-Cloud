package com.xypai.user.insurance.web;

import com.xypai.user.application.command.FollowUserCommand;
import com.xypai.user.application.command.UnfollowUserCommand;
import com.xypai.user.application.service.SocialApplicationService;
import com.xypai.user.domain.repository.SocialRepository;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.insurance.dto.SocialStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 社交控制器 - 接口层
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@RestController
@RequestMapping("/api/social")
@Tag(name = "社交管理", description = "用户关注、粉丝等社交功能接口")
@RequiredArgsConstructor
public class SocialController {

    private final SocialApplicationService socialApplicationService;
    private final SocialRepository socialRepository;

    /**
     * 关注用户
     */
    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/{userId}/follow/{targetUserId}")
    public ResponseEntity<Void> followUser(
            @Parameter(description = "当前用户ID") @PathVariable Long userId,
            @Parameter(description = "目标用户ID") @PathVariable Long targetUserId) {

        log.info("关注用户请求: {} -> {}", userId, targetUserId);

        var command = new FollowUserCommand(UserId.of(userId), UserId.of(targetUserId));
        socialApplicationService.followUser(command);

        return ResponseEntity.ok().build();
    }

    /**
     * 取消关注用户
     */
    @Operation(summary = "取消关注用户", description = "取消关注指定用户")
    @DeleteMapping("/{userId}/follow/{targetUserId}")
    public ResponseEntity<Void> unfollowUser(
            @Parameter(description = "当前用户ID") @PathVariable Long userId,
            @Parameter(description = "目标用户ID") @PathVariable Long targetUserId) {

        log.info("取消关注用户请求: {} -> {}", userId, targetUserId);

        var command = new UnfollowUserCommand(UserId.of(userId), UserId.of(targetUserId));
        socialApplicationService.unfollowUser(command);

        return ResponseEntity.ok().build();
    }

    /**
     * 检查关注关系
     */
    @Operation(summary = "检查关注关系", description = "检查用户A是否关注用户B")
    @GetMapping("/{userId}/following/{targetUserId}")
    public ResponseEntity<Boolean> isFollowing(
            @Parameter(description = "当前用户ID") @PathVariable Long userId,
            @Parameter(description = "目标用户ID") @PathVariable Long targetUserId) {

        boolean isFollowing = socialApplicationService.isFollowing(
                UserId.of(userId), UserId.of(targetUserId));

        return ResponseEntity.ok(isFollowing);
    }

    /**
     * 获取社交统计
     */
    @Operation(summary = "获取社交统计", description = "获取用户的关注数和粉丝数")
    @GetMapping("/{userId}/stats")
    public ResponseEntity<SocialStatsResponse> getSocialStats(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        var stats = socialApplicationService.getSocialStats(UserId.of(userId));
        var response = new SocialStatsResponse(
                stats.followingCount(),
                stats.followerCount()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 获取关注列表
     */
    @Operation(summary = "获取关注列表", description = "获取用户关注的人列表")
    @GetMapping("/{userId}/following")
    public ResponseEntity<Object> getFollowing(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        // TODO: 实现分页获取关注列表
        log.info("获取关注列表请求: userId={}, page={}, size={}", userId, page, size);

        var socialAggregate = socialRepository.findByUserId(UserId.of(userId));
        if (socialAggregate.isEmpty()) {
            return ResponseEntity.ok("[]");
        }

        var followings = socialAggregate.get().getActiveFollowings();
        return ResponseEntity.ok(followings.size() + " followings");
    }

    /**
     * 获取粉丝列表
     */
    @Operation(summary = "获取粉丝列表", description = "获取用户的粉丝列表")
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Object> getFollowers(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        // TODO: 实现分页获取粉丝列表
        log.info("获取粉丝列表请求: userId={}, page={}, size={}", userId, page, size);

        var socialAggregate = socialRepository.findByUserId(UserId.of(userId));
        if (socialAggregate.isEmpty()) {
            return ResponseEntity.ok("[]");
        }

        var followers = socialAggregate.get().getActiveFollowers();
        return ResponseEntity.ok(followers.size() + " followers");
    }
}