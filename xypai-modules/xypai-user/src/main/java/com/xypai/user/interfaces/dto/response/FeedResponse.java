package com.xypai.user.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xypai.user.domain.feed.FeedAggregate;
import com.xypai.user.domain.feed.entity.FeedSettings;
import com.xypai.user.domain.feed.enums.FeedStatus;
import com.xypai.user.domain.feed.enums.FeedType;
import com.xypai.user.domain.feed.valueobject.FeedId;
import com.xypai.user.domain.user.valueobject.UserId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 动态响应DTO
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Schema(description = "动态信息")
public record FeedResponse(
        @Schema(description = "动态ID", example = "feed_123")
        FeedId feedId,

        @Schema(description = "作者用户ID", example = "1")
        UserId authorId,

        @Schema(description = "动态类型", example = "TEXT")
        FeedType type,

        @Schema(description = "动态状态", example = "PUBLISHED")
        FeedStatus status,

        @Schema(description = "文本内容", example = "今天天气不错!")
        String textContent,

        @Schema(description = "媒体URL列表")
        List<String> mediaUrls,

        @Schema(description = "链接URL")
        String linkUrl,

        @Schema(description = "链接标题")
        String linkTitle,

        @Schema(description = "链接描述")
        String linkDescription,

        @Schema(description = "位置信息")
        String location,

        @Schema(description = "话题标签")
        List<String> hashtags,

        @Schema(description = "提及用户")
        List<String> mentions,

        @Schema(description = "动态设置")
        FeedSettingsDto settings,

        @Schema(description = "浏览次数", example = "128")
        int viewCount,

        @Schema(description = "分享次数", example = "12")
        int shareCount,

        @Schema(description = "内容摘要")
        String summary,

        @Schema(description = "创建时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createTime,

        @Schema(description = "更新时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateTime,

        @Schema(description = "发布时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishTime
) {

    public FeedResponse {
        Objects.requireNonNull(feedId, "动态ID不能为空");
        Objects.requireNonNull(authorId, "作者ID不能为空");
        Objects.requireNonNull(type, "动态类型不能为空");
        Objects.requireNonNull(status, "动态状态不能为空");
        Objects.requireNonNull(createTime, "创建时间不能为空");
    }

    /**
     * 静态工厂方法：从FeedAggregate创建FeedResponse
     */
    public static FeedResponse fromAggregate(FeedAggregate aggregate) {
        Objects.requireNonNull(aggregate, "FeedAggregate不能为空");

        var content = aggregate.getContent();
        var settings = aggregate.getSettings();

        return new FeedResponse(
                aggregate.getFeedId(),
                aggregate.getAuthorId(),
                content.getType(),
                aggregate.getStatus(),
                content.getTextContent(),
                content.getMediaUrls(),
                content.getLinkUrl(),
                content.getLinkTitle(),
                content.getLinkDescription(),
                content.getLocation(),
                content.getHashtags(),
                content.getMentions(),
                FeedSettingsDto.fromSettings(settings),
                aggregate.getViewCount(),
                aggregate.getShareCount(),
                aggregate.getSummary(),
                aggregate.getCreateTime(),
                aggregate.getUpdateTime(),
                aggregate.getPublishTime()
        );
    }

    /**
     * 动态设置DTO
     */
    @Schema(description = "动态设置")
    public record FeedSettingsDto(
            @Schema(description = "是否允许评论", example = "true")
            boolean allowComments,

            @Schema(description = "是否允许点赞", example = "true")
            boolean allowLikes,

            @Schema(description = "是否允许分享", example = "true")
            boolean allowShares,

            @Schema(description = "可见性级别", example = "PUBLIC")
            String visibility,

            @Schema(description = "是否启用通知", example = "true")
            boolean enableNotifications
    ) {

        public static FeedSettingsDto fromSettings(FeedSettings settings) {
            return new FeedSettingsDto(
                    settings.isAllowComments(),
                    settings.isAllowLikes(),
                    settings.isAllowShares(),
                    settings.getVisibility().getCode(),
                    settings.isEnableNotifications()
            );
        }

        public FeedSettings toSettings() {
            return FeedSettings.customSettings(
                    allowComments,
                    allowLikes,
                    allowShares,
                    FeedSettings.VisibilityLevel.fromCode(visibility),
                    enableNotifications
            );
        }
    }
}
