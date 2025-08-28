package com.xypai.user.application.feed;

import com.xypai.user.application.feed.command.CreateFeedCommand;
import com.xypai.user.application.feed.command.PublishFeedCommand;
import com.xypai.user.application.feed.command.UpdateFeedCommand;
import com.xypai.user.application.feed.command.UpdateFeedSettingsCommand;
import com.xypai.user.domain.feed.FeedAggregate;
import com.xypai.user.domain.feed.entity.FeedContent;
import com.xypai.user.domain.feed.entity.FeedSettings;
import com.xypai.user.domain.feed.enums.FeedStatus;
import com.xypai.user.domain.feed.repository.FeedRepository;
import com.xypai.user.domain.feed.valueobject.FeedId;
import com.xypai.user.domain.shared.service.DomainEventPublisher;
import com.xypai.user.domain.user.valueobject.UserId;
import com.xypai.user.insurance.dto.feed.FeedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 动态应用服务 - 编排动态业务流程
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedApplicationService {

    private final FeedRepository feedRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * 创建动态（草稿状态）
     */
    @Transactional
    public FeedResponse createFeed(CreateFeedCommand command) {
        log.info("创建动态开始: {}", command);

        // 创建动态内容
        var content = createFeedContent(command);

        // 创建动态设置
        var settings = command.settings() != null ? command.settings() : FeedSettings.defaultSettings();

        // 创建聚合根
        var feedAggregate = FeedAggregate.createDraft(command.authorId(), content, settings);

        // 保存聚合根
        var savedAggregate = feedRepository.save(feedAggregate);

        log.info("创建动态完成: feedId={}, authorId={}", savedAggregate.getFeedId(), command.authorId());
        return FeedResponse.fromAggregate(savedAggregate);
    }

    /**
     * 直接发布动态
     */
    @Transactional
    public FeedResponse createAndPublishFeed(CreateFeedCommand command) {
        log.info("创建并发布动态开始: {}", command);

        // 创建动态内容
        var content = createFeedContent(command);

        // 创建动态设置
        var settings = command.settings() != null ? command.settings() : FeedSettings.defaultSettings();

        // 创建并发布聚合根
        var feedAggregate = FeedAggregate.createAndPublish(command.authorId(), content, settings);

        // 保存聚合根
        var savedAggregate = feedRepository.save(feedAggregate);

        // 发布领域事件
        eventPublisher.publishAll(savedAggregate.getDomainEvents());
        savedAggregate.clearDomainEvents();

        log.info("创建并发布动态完成: feedId={}, authorId={}", savedAggregate.getFeedId(), command.authorId());
        return FeedResponse.fromAggregate(savedAggregate);
    }

    /**
     * 发布动态
     */
    @Transactional
    public void publishFeed(PublishFeedCommand command) {
        log.info("发布动态开始: {}", command);

        // 获取聚合根
        var feedAggregate = feedRepository.findById(command.feedId())
                .orElseThrow(() -> new IllegalArgumentException("动态不存在"));

        // 验证权限
        if (!feedAggregate.getAuthorId().equals(command.authorId())) {
            throw new IllegalArgumentException("无权限发布该动态");
        }

        // 执行发布业务逻辑
        var event = feedAggregate.publish();

        // 保存聚合根
        feedRepository.save(feedAggregate);

        // 发布领域事件
        if (event != null) {
            eventPublisher.publish(event);
        }
        feedAggregate.clearDomainEvents();

        log.info("发布动态完成: feedId={}", command.feedId());
    }

    /**
     * 更新动态内容
     */
    @Transactional
    public FeedResponse updateFeed(UpdateFeedCommand command) {
        log.info("更新动态开始: {}", command);

        // 获取聚合根
        var feedAggregate = feedRepository.findById(command.feedId())
                .orElseThrow(() -> new IllegalArgumentException("动态不存在"));

        // 验证权限
        if (!feedAggregate.getAuthorId().equals(command.authorId())) {
            throw new IllegalArgumentException("无权限更新该动态");
        }

        // 创建新的动态内容
        var newContent = createFeedContent(command);

        // 执行更新业务逻辑
        feedAggregate.updateContent(newContent);

        // 保存聚合根
        var savedAggregate = feedRepository.save(feedAggregate);

        // 发布领域事件
        eventPublisher.publishAll(savedAggregate.getDomainEvents());
        savedAggregate.clearDomainEvents();

        log.info("更新动态完成: feedId={}", command.feedId());
        return FeedResponse.fromAggregate(savedAggregate);
    }

    /**
     * 更新动态设置
     */
    @Transactional
    public void updateFeedSettings(UpdateFeedSettingsCommand command) {
        log.info("更新动态设置开始: {}", command);

        // 获取聚合根
        var feedAggregate = feedRepository.findById(command.feedId())
                .orElseThrow(() -> new IllegalArgumentException("动态不存在"));

        // 验证权限
        if (!feedAggregate.getAuthorId().equals(command.authorId())) {
            throw new IllegalArgumentException("无权限更新该动态");
        }

        // 执行更新业务逻辑
        feedAggregate.updateSettings(command.settings());

        // 保存聚合根
        feedRepository.save(feedAggregate);

        log.info("更新动态设置完成: feedId={}", command.feedId());
    }

    /**
     * 删除动态
     */
    @Transactional
    public void deleteFeed(FeedId feedId, UserId authorId) {
        log.info("删除动态开始: feedId={}, authorId={}", feedId, authorId);

        // 获取聚合根
        var feedAggregate = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("动态不存在"));

        // 验证权限
        if (!feedAggregate.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("无权限删除该动态");
        }

        // 执行删除业务逻辑
        feedAggregate.delete();

        // 保存聚合根
        feedRepository.save(feedAggregate);

        log.info("删除动态完成: feedId={}", feedId);
    }

    /**
     * 增加浏览次数
     */
    @Transactional
    public void incrementViewCount(FeedId feedId) {
        var feedAggregate = feedRepository.findById(feedId);
        if (feedAggregate.isPresent()) {
            feedAggregate.get().incrementViewCount();
            feedRepository.save(feedAggregate.get());
        }
    }

    /**
     * 增加分享次数
     */
    @Transactional
    public void incrementShareCount(FeedId feedId) {
        var feedAggregate = feedRepository.findById(feedId);
        if (feedAggregate.isPresent()) {
            feedAggregate.get().incrementShareCount();
            feedRepository.save(feedAggregate.get());
        }
    }

    // ========================================
    // 查询方法
    // ========================================

    /**
     * 根据ID查询动态
     */
    @Transactional(readOnly = true)
    public Optional<FeedResponse> findFeedById(FeedId feedId) {
        return feedRepository.findById(feedId)
                .map(FeedResponse::fromAggregate);
    }

    /**
     * 查询用户动态列表
     */
    @Transactional(readOnly = true)
    public List<FeedResponse> findUserFeeds(UserId authorId, FeedStatus status) {
        var feeds = status != null
                ? feedRepository.findByAuthorIdAndStatus(authorId, status)
                : feedRepository.findByAuthorId(authorId);

        return feeds.stream()
                .map(FeedResponse::fromAggregate)
                .collect(Collectors.toList());
    }

    /**
     * 查询用户时间线
     */
    @Transactional(readOnly = true)
    public List<FeedResponse> findUserTimeline(UserId userId, LocalDateTime before, int limit) {
        var feeds = feedRepository.findTimelineByUserId(userId, before, limit);
        return feeds.stream()
                .map(FeedResponse::fromAggregate)
                .collect(Collectors.toList());
    }

    /**
     * 查询公开动态
     */
    @Transactional(readOnly = true)
    public List<FeedResponse> findPublicFeeds(LocalDateTime before, int limit) {
        var feeds = feedRepository.findPublicFeeds(before, limit);
        return feeds.stream()
                .map(FeedResponse::fromAggregate)
                .collect(Collectors.toList());
    }

    /**
     * 查询关注动态
     */
    @Transactional(readOnly = true)
    public List<FeedResponse> findFollowingFeeds(UserId userId, LocalDateTime before, int limit) {
        var feeds = feedRepository.findFollowingFeeds(userId, before, limit);
        return feeds.stream()
                .map(FeedResponse::fromAggregate)
                .collect(Collectors.toList());
    }

    /**
     * 根据话题查询动态
     */
    @Transactional(readOnly = true)
    public List<FeedResponse> findFeedsByHashtag(String hashtag, LocalDateTime before, int limit) {
        var feeds = feedRepository.findByHashtag(hashtag, before, limit);
        return feeds.stream()
                .map(FeedResponse::fromAggregate)
                .collect(Collectors.toList());
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    private FeedContent createFeedContent(CreateFeedCommand command) {
        switch (command.type()) {
            case TEXT:
                return FeedContent.createTextContent(command.textContent());
            case IMAGE:
                return FeedContent.createImageContent(command.textContent(), command.mediaUrls());
            case VIDEO:
                return FeedContent.createVideoContent(command.textContent(),
                        command.mediaUrls().isEmpty() ? null : command.mediaUrls().get(0));
            case LINK:
                return FeedContent.createLinkContent(command.textContent(), command.linkUrl(),
                        command.linkTitle(), command.linkDescription());
            case LOCATION:
                return FeedContent.createLocationContent(command.textContent(), command.location());
            default:
                throw new IllegalArgumentException("不支持的动态类型: " + command.type());
        }
    }

    private FeedContent createFeedContent(UpdateFeedCommand command) {
        switch (command.type()) {
            case TEXT:
                return FeedContent.createTextContent(command.textContent());
            case IMAGE:
                return FeedContent.createImageContent(command.textContent(), command.mediaUrls());
            case VIDEO:
                return FeedContent.createVideoContent(command.textContent(),
                        command.mediaUrls().isEmpty() ? null : command.mediaUrls().get(0));
            case LINK:
                return FeedContent.createLinkContent(command.textContent(), command.linkUrl(),
                        command.linkTitle(), command.linkDescription());
            case LOCATION:
                return FeedContent.createLocationContent(command.textContent(), command.location());
            default:
                throw new IllegalArgumentException("不支持的动态类型: " + command.type());
        }
    }
}
