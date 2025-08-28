package com.xypai.user.application.feed.command;

import com.xypai.user.domain.feed.enums.FeedType;
import com.xypai.user.domain.feed.valueobject.FeedId;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 更新动态命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UpdateFeedCommand(
        @NotNull(message = "动态ID不能为空")
        FeedId feedId,

        @NotNull(message = "作者ID不能为空")
        UserId authorId,

        @NotNull(message = "动态类型不能为空")
        FeedType type,

        @Size(max = 2000, message = "文本内容不能超过2000字符")
        String textContent,

        List<String> mediaUrls,

        String linkUrl,

        String linkTitle,

        String linkDescription,

        String location,

        List<String> hashtags,

        List<String> mentions
) {

    public UpdateFeedCommand {
        // 根据类型验证必填字段
        validateByType(type, textContent, mediaUrls, linkUrl, location);
    }

    private static void validateByType(FeedType type, String textContent,
                                       List<String> mediaUrls, String linkUrl, String location) {
        switch (type) {
            case TEXT:
                if (textContent == null || textContent.trim().isEmpty()) {
                    throw new IllegalArgumentException("文本动态必须包含文本内容");
                }
                break;
            case IMAGE:
                if (mediaUrls == null || mediaUrls.isEmpty()) {
                    throw new IllegalArgumentException("图片动态必须包含至少一张图片");
                }
                if (mediaUrls.size() > 9) {
                    throw new IllegalArgumentException("图片动态最多支持9张图片");
                }
                break;
            case VIDEO:
                if (mediaUrls == null || mediaUrls.isEmpty()) {
                    throw new IllegalArgumentException("视频动态必须包含视频文件");
                }
                if (mediaUrls.size() > 1) {
                    throw new IllegalArgumentException("视频动态只能包含一个视频文件");
                }
                break;
            case LINK:
                if (linkUrl == null || linkUrl.trim().isEmpty()) {
                    throw new IllegalArgumentException("链接分享必须包含链接URL");
                }
                break;
            case LOCATION:
                if (location == null || location.trim().isEmpty()) {
                    throw new IllegalArgumentException("位置打卡必须包含位置信息");
                }
                break;
            case TOPIC:
            case ACTIVITY:
                // 这些类型由其他聚合根处理，这里只做基本验证
                if (textContent == null || textContent.trim().isEmpty()) {
                    throw new IllegalArgumentException("话题和活动动态必须包含文本内容");
                }
                break;
        }
    }
}
