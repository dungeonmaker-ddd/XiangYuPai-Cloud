package com.xypai.user.domain.enums;

/**
 * 动态类型枚举
 *
 * @author XyPai
 * @since 2025-01-02
 */
public enum FeedType {

    /**
     * 文本动态
     */
    TEXT("text", "文本动态"),

    /**
     * 图片动态
     */
    IMAGE("image", "图片动态"),

    /**
     * 视频动态
     */
    VIDEO("video", "视频动态"),

    /**
     * 链接分享
     */
    LINK("link", "链接分享"),

    /**
     * 位置打卡
     */
    LOCATION("location", "位置打卡"),

    /**
     * 话题讨论
     */
    TOPIC("topic", "话题讨论"),

    /**
     * 活动发布
     */
    ACTIVITY("activity", "活动发布");

    private final String code;
    private final String description;

    FeedType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static FeedType fromCode(String code) {
        for (FeedType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的动态类型: " + code);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否为多媒体类型
     */
    public boolean isMultimedia() {
        return this == IMAGE || this == VIDEO;
    }

    /**
     * 检查是否需要特殊处理
     */
    public boolean requiresSpecialHandling() {
        return this == ACTIVITY || this == TOPIC;
    }
}
