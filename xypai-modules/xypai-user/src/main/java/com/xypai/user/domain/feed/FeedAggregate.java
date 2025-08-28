package com.xypai.user.domain.feed;

import com.xypai.user.domain.feed.entity.FeedContent;
import com.xypai.user.domain.feed.entity.FeedSettings;
import com.xypai.user.domain.feed.enums.FeedStatus;
import com.xypai.user.domain.feed.valueobject.FeedId;
import com.xypai.user.domain.shared.DomainEvent;
import com.xypai.user.domain.shared.FeedPublishedEvent;
import com.xypai.user.domain.user.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 动态聚合根 - 处理用户动态发布和管理
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class FeedAggregate {

    // ========================================
    // 聚合根标识
    // ========================================

    private final FeedId feedId;
    private final UserId authorId;              // 作者用户ID

    // ========================================
    // 动态基本信息
    // ========================================
    private final LocalDateTime createTime;    // 创建时间
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private FeedContent content;                // 动态内容

    // ========================================
    // 时间信息
    // ========================================
    private FeedStatus status;                  // 动态状态
    private FeedSettings settings;             // 动态设置
    private LocalDateTime updateTime;          // 更新时间

    // ========================================
    // 统计信息
    // ========================================
    private LocalDateTime publishTime;         // 发布时间
    private int viewCount;                     // 浏览次数

    // ========================================
    // 领域事件
    // ========================================
    private int shareCount;                    // 分享次数

    // ========================================
    // 构造器
    // ========================================

    private FeedAggregate(FeedId feedId, UserId authorId, FeedContent content,
                          FeedStatus status, FeedSettings settings,
                          LocalDateTime createTime, LocalDateTime updateTime,
                          LocalDateTime publishTime, int viewCount, int shareCount) {
        this.feedId = Objects.requireNonNull(feedId, "动态ID不能为空");
        this.authorId = Objects.requireNonNull(authorId, "作者ID不能为空");
        this.content = Objects.requireNonNull(content, "动态内容不能为空");
        this.status = Objects.requireNonNull(status, "动态状态不能为空");
        this.settings = Objects.requireNonNull(settings, "动态设置不能为空");
        this.createTime = Objects.requireNonNull(createTime, "创建时间不能为空");
        this.updateTime = updateTime;
        this.publishTime = publishTime;
        this.viewCount = Math.max(0, viewCount);
        this.shareCount = Math.max(0, shareCount);
    }

    // ========================================
    // 工厂方法
    // ========================================

    /**
     * 创建新的动态聚合根（草稿状态）
     */
    public static FeedAggregate createDraft(UserId authorId, FeedContent content, FeedSettings settings) {
        var feedId = FeedId.generate();
        var now = LocalDateTime.now();

        return new FeedAggregate(
                feedId, authorId, content, FeedStatus.DRAFT, settings,
                now, now, null, 0, 0
        );
    }

    /**
     * 直接发布动态
     */
    public static FeedAggregate createAndPublish(UserId authorId, FeedContent content, FeedSettings settings) {
        var aggregate = createDraft(authorId, content, settings);
        aggregate.publish();
        return aggregate;
    }

    /**
     * 从现有数据重建聚合根
     */
    public static FeedAggregate fromExisting(
            FeedId feedId, UserId authorId, FeedContent content,
            FeedStatus status, FeedSettings settings,
            LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime publishTime,
            int viewCount, int shareCount) {

        return new FeedAggregate(
                feedId, authorId, content, status, settings,
                createTime, updateTime, publishTime, viewCount, shareCount
        );
    }

    // ========================================
    // 业务方法 - 内容管理
    // ========================================

    /**
     * 更新动态内容
     */
    public void updateContent(FeedContent newContent) {
        // 验证业务规则
        validateCanEdit();
        Objects.requireNonNull(newContent, "动态内容不能为空");

        // 执行业务逻辑
        this.content = newContent;
        this.updateTime = LocalDateTime.now();

        // 如果内容包含敏感信息，需要重新审核
        if (newContent.containsSensitiveContent() && status == FeedStatus.PUBLISHED) {
            this.status = FeedStatus.REVIEWING;
        }
    }

    /**
     * 更新动态设置
     */
    public void updateSettings(FeedSettings newSettings) {
        Objects.requireNonNull(newSettings, "动态设置不能为空");
        this.settings = newSettings;
        this.updateTime = LocalDateTime.now();
    }

    // ========================================
    // 业务方法 - 状态管理
    // ========================================

    /**
     * 发布动态
     */
    public DomainEvent publish() {
        // 验证业务规则
        validateCanPublish();

        // 执行业务逻辑
        this.status = content.containsSensitiveContent() ? FeedStatus.REVIEWING : FeedStatus.PUBLISHED;
        this.publishTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();

        // 生成领域事件
        if (status == FeedStatus.PUBLISHED) {
            var event = FeedPublishedEvent.create(feedId, authorId, content.getType());
            addDomainEvent(event);
            return event;
        }

        return null; // 如果进入审核状态，暂不发布事件
    }

    /**
     * 隐藏动态
     */
    public void hide() {
        validateCanHide();
        this.status = FeedStatus.HIDDEN;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 显示动态
     */
    public void show() {
        validateCanShow();
        this.status = FeedStatus.PUBLISHED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 删除动态
     */
    public void delete() {
        validateCanDelete();
        this.status = FeedStatus.DELETED;
        this.updateTime = LocalDateTime.now();
        // TODO: 可以发布动态删除事件
    }

    /**
     * 审核通过
     */
    public DomainEvent approve() {
        validateCanApprove();
        this.status = FeedStatus.PUBLISHED;
        this.updateTime = LocalDateTime.now();

        var event = FeedPublishedEvent.create(feedId, authorId, content.getType());
        addDomainEvent(event);
        return event;
    }

    /**
     * 审核不通过
     */
    public void reject(String reason) {
        validateCanReject();
        this.status = FeedStatus.REJECTED;
        this.updateTime = LocalDateTime.now();
        // TODO: 可以发布审核不通过事件，包含原因
    }

    // ========================================
    // 业务方法 - 统计管理
    // ========================================

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        if (status == FeedStatus.PUBLISHED) {
            this.viewCount++;
        }
    }

    /**
     * 增加分享次数
     */
    public void incrementShareCount() {
        if (status == FeedStatus.PUBLISHED && settings.isAllowShares()) {
            this.shareCount++;
        }
    }

    // ========================================
    // 查询方法
    // ========================================

    /**
     * 检查是否为公开可见状态
     */
    public boolean isVisible() {
        return status.isVisible() && settings.isPublic();
    }

    /**
     * 检查是否可以被指定用户查看
     */
    public boolean isVisibleTo(UserId viewerId) {
        // 作者总是可以查看自己的动态
        if (authorId.equals(viewerId)) {
            return true;
        }

        // 检查动态状态
        if (!status.isVisible()) {
            return false;
        }

        // 检查可见性设置
        switch (settings.getVisibility()) {
            case PUBLIC:
                return true;
            case FOLLOWERS_ONLY:
                // TODO: 需要检查关注关系，这里暂时返回false
                return false;
            case FRIENDS_ONLY:
                // TODO: 需要检查好友关系，这里暂时返回false
                return false;
            case PRIVATE:
                return false;
            default:
                return false;
        }
    }

    /**
     * 检查是否允许点赞
     */
    public boolean allowsLikes() {
        return status.isVisible() && settings.isAllowLikes();
    }

    /**
     * 检查是否允许评论
     */
    public boolean allowsComments() {
        return status.isVisible() && settings.isAllowComments();
    }

    /**
     * 检查是否允许分享
     */
    public boolean allowsShares() {
        return status.isVisible() && settings.isAllowShares();
    }

    /**
     * 获取动态摘要
     */
    public String getSummary() {
        return content.getSummary();
    }

    // ========================================
    // 业务规则验证
    // ========================================

    private void validateCanEdit() {
        if (!status.isEditable()) {
            throw new IllegalStateException("当前状态不允许编辑: " + status);
        }
    }

    private void validateCanPublish() {
        if (status != FeedStatus.DRAFT && status != FeedStatus.REJECTED) {
            throw new IllegalStateException("只有草稿或审核不通过的动态才能发布: " + status);
        }
    }

    private void validateCanHide() {
        if (status != FeedStatus.PUBLISHED) {
            throw new IllegalStateException("只有已发布的动态才能隐藏: " + status);
        }
    }

    private void validateCanShow() {
        if (status != FeedStatus.HIDDEN) {
            throw new IllegalStateException("只有已隐藏的动态才能显示: " + status);
        }
    }

    private void validateCanDelete() {
        if (status.isFinal()) {
            throw new IllegalStateException("动态已删除，无法再次删除: " + status);
        }
    }

    private void validateCanApprove() {
        if (status != FeedStatus.REVIEWING) {
            throw new IllegalStateException("只有审核中的动态才能通过审核: " + status);
        }
    }

    private void validateCanReject() {
        if (status != FeedStatus.REVIEWING) {
            throw new IllegalStateException("只有审核中的动态才能拒绝审核: " + status);
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

    public FeedId getFeedId() {
        return feedId;
    }

    public UserId getAuthorId() {
        return authorId;
    }

    public FeedContent getContent() {
        return content;
    }

    public FeedStatus getStatus() {
        return status;
    }

    public FeedSettings getSettings() {
        return settings;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    @Override
    public String toString() {
        return String.format("FeedAggregate{feedId=%s, authorId=%s, status=%s, summary='%s'}",
                feedId, authorId, status, getSummary());
    }
}
