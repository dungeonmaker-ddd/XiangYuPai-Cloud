package com.xypai.user.domain.feed.enums;

/**
 * 动态状态枚举
 *
 * @author XyPai
 * @since 2025-01-02
 */
public enum FeedStatus {

    /**
     * 草稿状态
     */
    DRAFT("draft", "草稿"),

    /**
     * 已发布
     */
    PUBLISHED("published", "已发布"),

    /**
     * 已隐藏
     */
    HIDDEN("hidden", "已隐藏"),

    /**
     * 已删除
     */
    DELETED("deleted", "已删除"),

    /**
     * 审核中
     */
    REVIEWING("reviewing", "审核中"),

    /**
     * 审核不通过
     */
    REJECTED("rejected", "审核不通过");

    private final String code;
    private final String description;

    FeedStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static FeedStatus fromCode(String code) {
        for (FeedStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的动态状态: " + code);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否为公开可见状态
     */
    public boolean isVisible() {
        return this == PUBLISHED;
    }

    /**
     * 检查是否可以编辑
     */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }

    /**
     * 检查是否为最终状态
     */
    public boolean isFinal() {
        return this == DELETED;
    }
}
