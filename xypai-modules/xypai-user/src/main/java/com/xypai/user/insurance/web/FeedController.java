package com.xypai.user.insurance.web;

import com.xypai.user.application.command.CreateFeedCommand;
import com.xypai.user.application.command.PublishFeedCommand;
import com.xypai.user.application.command.UpdateFeedCommand;
import com.xypai.user.application.command.UpdateFeedSettingsCommand;
import com.xypai.user.application.service.FeedApplicationService;
import com.xypai.user.domain.enums.FeedStatus;
import com.xypai.user.domain.enums.FeedType;
import com.xypai.user.domain.valueobject.FeedId;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.insurance.dto.FeedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态控制器 - 接口层
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@RestController
@RequestMapping("/api/feeds")
@Tag(name = "动态管理", description = "用户动态发布、管理等功能接口")
@RequiredArgsConstructor
public class FeedController {

    private final FeedApplicationService feedApplicationService;

    /**
     * 创建动态（草稿状态）
     */
    @Operation(summary = "创建动态", description = "创建动态，默认为草稿状态")
    @PostMapping
    public ResponseEntity<FeedResponse> createFeed(@Valid @RequestBody CreateFeedRequest request) {
        log.info("创建动态请求: authorId={}, type={}", request.authorId(), request.type());

        var command = new CreateFeedCommand(
                UserId.of(request.authorId()),
                FeedType.fromCode(request.type()),
                request.textContent(),
                request.mediaUrls(),
                request.linkUrl(),
                request.linkTitle(),
                request.linkDescription(),
                request.location(),
                request.hashtags(),
                request.mentions(),
                request.settings() != null ? request.settings().toSettings() : null
        );

        var response = feedApplicationService.createFeed(command);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 创建并发布动态
     */
    @Operation(summary = "创建并发布动态", description = "创建动态并立即发布")
    @PostMapping("/publish")
    public ResponseEntity<FeedResponse> createAndPublishFeed(@Valid @RequestBody CreateFeedRequest request) {
        log.info("创建并发布动态请求: authorId={}, type={}", request.authorId(), request.type());

        var command = new CreateFeedCommand(
                UserId.of(request.authorId()),
                FeedType.fromCode(request.type()),
                request.textContent(),
                request.mediaUrls(),
                request.linkUrl(),
                request.linkTitle(),
                request.linkDescription(),
                request.location(),
                request.hashtags(),
                request.mentions(),
                request.settings() != null ? request.settings().toSettings() : null
        );

        var response = feedApplicationService.createAndPublishFeed(command);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 发布动态
     */
    @Operation(summary = "发布动态", description = "发布草稿状态的动态")
    @PostMapping("/{feedId}/publish")
    public ResponseEntity<Void> publishFeed(
            @Parameter(description = "动态ID") @PathVariable String feedId,
            @Parameter(description = "作者用户ID") @RequestParam Long authorId) {

        log.info("发布动态请求: feedId={}, authorId={}", feedId, authorId);

        var command = new PublishFeedCommand(
                FeedId.of(feedId),
                UserId.of(authorId)
        );

        feedApplicationService.publishFeed(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新动态内容
     */
    @Operation(summary = "更新动态内容", description = "更新动态的内容信息")
    @PutMapping("/{feedId}")
    public ResponseEntity<FeedResponse> updateFeed(
            @Parameter(description = "动态ID") @PathVariable String feedId,
            @Valid @RequestBody UpdateFeedRequest request) {

        log.info("更新动态请求: feedId={}, authorId={}", feedId, request.authorId());

        var command = new UpdateFeedCommand(
                FeedId.of(feedId),
                UserId.of(request.authorId()),
                FeedType.fromCode(request.type()),
                request.textContent(),
                request.mediaUrls(),
                request.linkUrl(),
                request.linkTitle(),
                request.linkDescription(),
                request.location(),
                request.hashtags(),
                request.mentions()
        );

        var response = feedApplicationService.updateFeed(command);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新动态设置
     */
    @Operation(summary = "更新动态设置", description = "更新动态的可见性和交互设置")
    @PutMapping("/{feedId}/settings")
    public ResponseEntity<Void> updateFeedSettings(
            @Parameter(description = "动态ID") @PathVariable String feedId,
            @Valid @RequestBody UpdateFeedSettingsRequest request) {

        log.info("更新动态设置请求: feedId={}, authorId={}", feedId, request.authorId());

        var command = new UpdateFeedSettingsCommand(
                FeedId.of(feedId),
                UserId.of(request.authorId()),
                request.settings().toSettings()
        );

        feedApplicationService.updateFeedSettings(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除动态
     */
    @Operation(summary = "删除动态", description = "删除指定的动态")
    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(
            @Parameter(description = "动态ID") @PathVariable String feedId,
            @Parameter(description = "作者用户ID") @RequestParam Long authorId) {

        log.info("删除动态请求: feedId={}, authorId={}", feedId, authorId);

        feedApplicationService.deleteFeed(FeedId.of(feedId), UserId.of(authorId));
        return ResponseEntity.ok().build();
    }

    /**
     * 查询动态详情
     */
    @Operation(summary = "查询动态详情", description = "根据ID查询动态详情")
    @GetMapping("/{feedId}")
    public ResponseEntity<FeedResponse> getFeed(@Parameter(description = "动态ID") @PathVariable String feedId) {
        var feed = feedApplicationService.findFeedById(FeedId.of(feedId));
        return feed.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询用户动态列表
     */
    @Operation(summary = "查询用户动态列表", description = "查询指定用户的动态列表")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedResponse>> getUserFeeds(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "动态状态") @RequestParam(required = false) String status) {

        FeedStatus feedStatus = status != null ? FeedStatus.fromCode(status) : null;
        var feeds = feedApplicationService.findUserFeeds(UserId.of(userId), feedStatus);
        return ResponseEntity.ok(feeds);
    }

    /**
     * 查询用户时间线
     */
    @Operation(summary = "查询用户时间线", description = "查询用户个人时间线")
    @GetMapping("/timeline/{userId}")
    public ResponseEntity<List<FeedResponse>> getUserTimeline(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "时间点之前") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime before,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {

        var feeds = feedApplicationService.findUserTimeline(
                UserId.of(userId),
                before != null ? before : LocalDateTime.now(),
                limit
        );
        return ResponseEntity.ok(feeds);
    }

    /**
     * 查询公开动态
     */
    @Operation(summary = "查询公开动态", description = "查询所有公开动态")
    @GetMapping("/public")
    public ResponseEntity<List<FeedResponse>> getPublicFeeds(
            @Parameter(description = "时间点之前") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime before,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {

        var feeds = feedApplicationService.findPublicFeeds(
                before != null ? before : LocalDateTime.now(),
                limit
        );
        return ResponseEntity.ok(feeds);
    }

    /**
     * 查询关注动态
     */
    @Operation(summary = "查询关注动态", description = "查询关注用户的动态")
    @GetMapping("/following/{userId}")
    public ResponseEntity<List<FeedResponse>> getFollowingFeeds(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "时间点之前") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime before,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {

        var feeds = feedApplicationService.findFollowingFeeds(
                UserId.of(userId),
                before != null ? before : LocalDateTime.now(),
                limit
        );
        return ResponseEntity.ok(feeds);
    }

    /**
     * 根据话题查询动态
     */
    @Operation(summary = "根据话题查询动态", description = "查询包含指定话题标签的动态")
    @GetMapping("/hashtag/{hashtag}")
    public ResponseEntity<List<FeedResponse>> getFeedsByHashtag(
            @Parameter(description = "话题标签") @PathVariable String hashtag,
            @Parameter(description = "时间点之前") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime before,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {

        var feeds = feedApplicationService.findFeedsByHashtag(
                hashtag,
                before != null ? before : LocalDateTime.now(),
                limit
        );
        return ResponseEntity.ok(feeds);
    }

    /**
     * 增加浏览次数
     */
    @Operation(summary = "增加浏览次数", description = "记录动态浏览")
    @PostMapping("/{feedId}/view")
    public ResponseEntity<Void> incrementViewCount(@Parameter(description = "动态ID") @PathVariable String feedId) {
        feedApplicationService.incrementViewCount(FeedId.of(feedId));
        return ResponseEntity.ok().build();
    }

    /**
     * 增加分享次数
     */
    @Operation(summary = "增加分享次数", description = "记录动态分享")
    @PostMapping("/{feedId}/share")
    public ResponseEntity<Void> incrementShareCount(@Parameter(description = "动态ID") @PathVariable String feedId) {
        feedApplicationService.incrementShareCount(FeedId.of(feedId));
        return ResponseEntity.ok().build();
    }

    // ========================================
    // 请求DTO定义
    // ========================================

    /**
     * 创建动态请求DTO
     */
    public record CreateFeedRequest(
            Long authorId,
            String type,
            String textContent,
            List<String> mediaUrls,
            String linkUrl,
            String linkTitle,
            String linkDescription,
            String location,
            List<String> hashtags,
            List<String> mentions,
            FeedResponse.FeedSettingsDto settings
    ) {
    }

    /**
     * 更新动态请求DTO
     */
    public record UpdateFeedRequest(
            Long authorId,
            String type,
            String textContent,
            List<String> mediaUrls,
            String linkUrl,
            String linkTitle,
            String linkDescription,
            String location,
            List<String> hashtags,
            List<String> mentions
    ) {
    }

    /**
     * 更新动态设置请求DTO
     */
    public record UpdateFeedSettingsRequest(
            Long authorId,
            FeedResponse.FeedSettingsDto settings
    ) {
    }
}
