package com.xypai.user.domain.feed.entity;

import java.util.Objects;

/**
 * 动态设置实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class FeedSettings {

    private final boolean allowComments;      // 是否允许评论
    private final boolean allowLikes;         // 是否允许点赞
    private final boolean allowShares;        // 是否允许分享
    private final VisibilityLevel visibility; // 可见性级别
    private final boolean enableNotifications; // 是否启用通知

    private FeedSettings(boolean allowComments, boolean allowLikes, boolean allowShares,
                         VisibilityLevel visibility, boolean enableNotifications) {
        this.allowComments = allowComments;
        this.allowLikes = allowLikes;
        this.allowShares = allowShares;
        this.visibility = Objects.requireNonNull(visibility, "可见性级别不能为空");
        this.enableNotifications = enableNotifications;
    }

    /**
     * 创建默认设置
     */
    public static FeedSettings defaultSettings() {
        return new FeedSettings(true, true, true, VisibilityLevel.PUBLIC, true);
    }

    /**
     * 创建私密设置
     */
    public static FeedSettings privateSettings() {
        return new FeedSettings(false, true, false, VisibilityLevel.PRIVATE, false);
    }

    /**
     * 创建仅关注者可见设置
     */
    public static FeedSettings followersOnlySettings() {
        return new FeedSettings(true, true, true, VisibilityLevel.FOLLOWERS_ONLY, true);
    }

    /**
     * 创建自定义设置
     */
    public static FeedSettings customSettings(boolean allowComments, boolean allowLikes,
                                              boolean allowShares, VisibilityLevel visibility,
                                              boolean enableNotifications) {
        return new FeedSettings(allowComments, allowLikes, allowShares, visibility, enableNotifications);
    }

    /**
     * 更新评论设置
     */
    public FeedSettings withAllowComments(boolean allowComments) {
        return new FeedSettings(allowComments, allowLikes, allowShares, visibility, enableNotifications);
    }

    /**
     * 更新点赞设置
     */
    public FeedSettings withAllowLikes(boolean allowLikes) {
        return new FeedSettings(allowComments, allowLikes, allowShares, visibility, enableNotifications);
    }

    /**
     * 更新分享设置
     */
    public FeedSettings withAllowShares(boolean allowShares) {
        return new FeedSettings(allowComments, allowLikes, allowShares, visibility, enableNotifications);
    }

    /**
     * 更新可见性设置
     */
    public FeedSettings withVisibility(VisibilityLevel visibility) {
        return new FeedSettings(allowComments, allowLikes, allowShares, visibility, enableNotifications);
    }

    /**
     * 更新通知设置
     */
    public FeedSettings withEnableNotifications(boolean enableNotifications) {
        return new FeedSettings(allowComments, allowLikes, allowShares, visibility, enableNotifications);
    }

    /**
     * 检查是否允许互动
     */
    public boolean allowsInteraction() {
        return allowComments || allowLikes || allowShares;
    }

    /**
     * 检查是否为公开动态
     */
    public boolean isPublic() {
        return visibility == VisibilityLevel.PUBLIC;
    }

    /**
     * 检查是否为私密动态
     */
    public boolean isPrivate() {
        return visibility == VisibilityLevel.PRIVATE;
    }

    // ========================================
    // 可见性级别枚举
    // ========================================

    public boolean isAllowComments() {
        return allowComments;
    }

    // ========================================
    // Getters
    // ========================================

    public boolean isAllowLikes() {
        return allowLikes;
    }

    public boolean isAllowShares() {
        return allowShares;
    }

    public VisibilityLevel getVisibility() {
        return visibility;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedSettings that = (FeedSettings) o;
        return allowComments == that.allowComments &&
                allowLikes == that.allowLikes &&
                allowShares == that.allowShares &&
                enableNotifications == that.enableNotifications &&
                visibility == that.visibility;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowComments, allowLikes, allowShares, visibility, enableNotifications);
    }

    @Override
    public String toString() {
        return String.format("FeedSettings{visibility=%s, allowComments=%s, allowLikes=%s, allowShares=%s}",
                visibility, allowComments, allowLikes, allowShares);
    }

    public enum VisibilityLevel {
        /**
         * 公开 - 所有人可见
         */
        PUBLIC("public", "公开"),

        /**
         * 关注者可见
         */
        FOLLOWERS_ONLY("followers_only", "关注者可见"),

        /**
         * 好友可见
         */
        FRIENDS_ONLY("friends_only", "好友可见"),

        /**
         * 私密 - 仅自己可见
         */
        PRIVATE("private", "私密");

        private final String code;
        private final String description;

        VisibilityLevel(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public static VisibilityLevel fromCode(String code) {
            for (VisibilityLevel level : values()) {
                if (level.code.equals(code)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("未知的可见性级别: " + code);
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
