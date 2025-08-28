package com.xypai.user.domain.entity;

import com.xypai.user.domain.enums.FeedType;

import java.util.List;
import java.util.Objects;

/**
 * 动态内容实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class FeedContent {

    private final FeedType type;          // 动态类型
    private final String textContent;    // 文本内容
    private final List<String> mediaUrls; // 媒体URL列表（图片、视频等）
    private final String linkUrl;        // 链接URL
    private final String linkTitle;      // 链接标题
    private final String linkDescription; // 链接描述
    private final String location;       // 位置信息
    private final List<String> hashtags; // 话题标签
    private final List<String> mentions; // @用户列表

    private FeedContent(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "动态类型不能为空");
        this.textContent = builder.textContent;
        this.mediaUrls = builder.mediaUrls != null ? List.copyOf(builder.mediaUrls) : List.of();
        this.linkUrl = builder.linkUrl;
        this.linkTitle = builder.linkTitle;
        this.linkDescription = builder.linkDescription;
        this.location = builder.location;
        this.hashtags = builder.hashtags != null ? List.copyOf(builder.hashtags) : List.of();
        this.mentions = builder.mentions != null ? List.copyOf(builder.mentions) : List.of();

        // 验证内容
        validateContent();
    }

    /**
     * 创建文本动态内容
     */
    public static FeedContent createTextContent(String textContent) {
        return new Builder(FeedType.TEXT)
                .textContent(textContent)
                .build();
    }

    /**
     * 创建图片动态内容
     */
    public static FeedContent createImageContent(String textContent, List<String> imageUrls) {
        return new Builder(FeedType.IMAGE)
                .textContent(textContent)
                .mediaUrls(imageUrls)
                .build();
    }

    /**
     * 创建视频动态内容
     */
    public static FeedContent createVideoContent(String textContent, String videoUrl) {
        return new Builder(FeedType.VIDEO)
                .textContent(textContent)
                .mediaUrls(List.of(videoUrl))
                .build();
    }

    /**
     * 创建链接分享内容
     */
    public static FeedContent createLinkContent(String textContent, String linkUrl,
                                                String linkTitle, String linkDescription) {
        return new Builder(FeedType.LINK)
                .textContent(textContent)
                .linkUrl(linkUrl)
                .linkTitle(linkTitle)
                .linkDescription(linkDescription)
                .build();
    }

    /**
     * 创建位置打卡内容
     */
    public static FeedContent createLocationContent(String textContent, String location) {
        return new Builder(FeedType.LOCATION)
                .textContent(textContent)
                .location(location)
                .build();
    }

    /**
     * 验证内容完整性
     */
    private void validateContent() {
        switch (type) {
            case TEXT:
                if (textContent == null || textContent.trim().isEmpty()) {
                    throw new IllegalArgumentException("文本动态必须包含文本内容");
                }
                break;
            case IMAGE:
                if (mediaUrls.isEmpty()) {
                    throw new IllegalArgumentException("图片动态必须包含至少一张图片");
                }
                if (mediaUrls.size() > 9) {
                    throw new IllegalArgumentException("图片动态最多支持9张图片");
                }
                break;
            case VIDEO:
                if (mediaUrls.isEmpty()) {
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

        // 验证文本内容长度
        if (textContent != null && textContent.length() > 2000) {
            throw new IllegalArgumentException("动态文本内容不能超过2000字符");
        }
    }

    /**
     * 获取内容摘要
     */
    public String getSummary() {
        if (textContent != null && !textContent.isEmpty()) {
            return textContent.length() > 100 ? textContent.substring(0, 100) + "..." : textContent;
        }

        switch (type) {
            case IMAGE:
                return "[图片] " + mediaUrls.size() + "张";
            case VIDEO:
                return "[视频]";
            case LINK:
                return "[链接] " + (linkTitle != null ? linkTitle : linkUrl);
            case LOCATION:
                return "[位置] " + location;
            default:
                return "[" + type.getDescription() + "]";
        }
    }

    /**
     * 检查是否包含敏感内容（简单实现）
     */
    public boolean containsSensitiveContent() {
        // 这里可以接入更复杂的内容审核系统
        if (textContent != null) {
            String[] sensitiveWords = {"广告", "违法", "色情"};
            String lowerContent = textContent.toLowerCase();
            for (String word : sensitiveWords) {
                if (lowerContent.contains(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ========================================
    // Builder 模式
    // ========================================

    public FeedType getType() {
        return type;
    }

    // ========================================
    // Getters
    // ========================================

    public String getTextContent() {
        return textContent;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public String getLinkDescription() {
        return linkDescription;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public List<String> getMentions() {
        return mentions;
    }

    @Override
    public String toString() {
        return String.format("FeedContent{type=%s, summary='%s'}", type, getSummary());
    }

    public static class Builder {
        private final FeedType type;
        private String textContent;
        private List<String> mediaUrls;
        private String linkUrl;
        private String linkTitle;
        private String linkDescription;
        private String location;
        private List<String> hashtags;
        private List<String> mentions;

        public Builder(FeedType type) {
            this.type = type;
        }

        public Builder textContent(String textContent) {
            this.textContent = textContent;
            return this;
        }

        public Builder mediaUrls(List<String> mediaUrls) {
            this.mediaUrls = mediaUrls;
            return this;
        }

        public Builder linkUrl(String linkUrl) {
            this.linkUrl = linkUrl;
            return this;
        }

        public Builder linkTitle(String linkTitle) {
            this.linkTitle = linkTitle;
            return this;
        }

        public Builder linkDescription(String linkDescription) {
            this.linkDescription = linkDescription;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder hashtags(List<String> hashtags) {
            this.hashtags = hashtags;
            return this;
        }

        public Builder mentions(List<String> mentions) {
            this.mentions = mentions;
            return this;
        }

        public FeedContent build() {
            return new FeedContent(this);
        }
    }
}
