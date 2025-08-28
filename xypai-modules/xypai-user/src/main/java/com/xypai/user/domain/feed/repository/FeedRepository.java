package com.xypai.user.domain.feed.repository;

import com.xypai.user.domain.feed.FeedAggregate;
import com.xypai.user.domain.feed.enums.FeedStatus;
import com.xypai.user.domain.feed.enums.FeedType;
import com.xypai.user.domain.feed.valueobject.FeedId;
import com.xypai.user.domain.user.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 动态仓储接口 - 领域层
 *
 * @author XyPai
 * @since 2025-01-02
 */
public interface FeedRepository {

    /**
     * 保存动态聚合根
     */
    FeedAggregate save(FeedAggregate feedAggregate);

    /**
     * 根据动态ID查找
     */
    Optional<FeedAggregate> findById(FeedId feedId);

    /**
     * 根据作者ID查找动态列表
     */
    List<FeedAggregate> findByAuthorId(UserId authorId);

    /**
     * 根据作者ID和状态查找动态列表
     */
    List<FeedAggregate> findByAuthorIdAndStatus(UserId authorId, FeedStatus status);

    /**
     * 根据动态类型查找动态列表
     */
    List<FeedAggregate> findByType(FeedType feedType, int limit);

    /**
     * 根据状态查找动态列表
     */
    List<FeedAggregate> findByStatus(FeedStatus status, int limit);

    /**
     * 查找用户时间线（按发布时间倒序）
     */
    List<FeedAggregate> findTimelineByUserId(UserId userId, LocalDateTime before, int limit);

    /**
     * 查找公开动态（热门推荐）
     */
    List<FeedAggregate> findPublicFeeds(LocalDateTime before, int limit);

    /**
     * 查找关注用户的动态
     */
    List<FeedAggregate> findFollowingFeeds(UserId userId, LocalDateTime before, int limit);

    /**
     * 根据话题标签查找动态
     */
    List<FeedAggregate> findByHashtag(String hashtag, LocalDateTime before, int limit);

    /**
     * 根据位置查找动态
     */
    List<FeedAggregate> findByLocation(String location, LocalDateTime before, int limit);

    /**
     * 查找用户的草稿动态
     */
    List<FeedAggregate> findDraftsByAuthorId(UserId authorId);

    /**
     * 查找待审核动态
     */
    List<FeedAggregate> findPendingReviewFeeds(int limit);

    /**
     * 统计用户动态数量
     */
    long countByAuthorId(UserId authorId);

    /**
     * 统计用户公开动态数量
     */
    long countPublishedByAuthorId(UserId authorId);

    /**
     * 检查动态是否存在
     */
    boolean existsById(FeedId feedId);

    /**
     * 删除动态聚合根
     */
    void delete(FeedId feedId);

    /**
     * 批量删除用户的所有动态
     */
    void deleteByAuthorId(UserId authorId);
}
