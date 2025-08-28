package com.xypai.user.domain.enums;

/**
 * 目标类型枚举
 *
 * @author XyPai
 * @since 2025-01-02
 */
public enum TargetType {

    /**
     * 用户动态
     */
    FEED("feed", "用户动态"),

    /**
     * 活动
     */
    ACTIVITY("activity", "活动"),

    /**
     * 评论
     */
    COMMENT("comment", "评论"),

    /**
     * 用户
     */
    USER("user", "用户");

    private final String code;
    private final String description;

    TargetType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static TargetType fromCode(String code) {
        for (TargetType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的目标类型: " + code);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
