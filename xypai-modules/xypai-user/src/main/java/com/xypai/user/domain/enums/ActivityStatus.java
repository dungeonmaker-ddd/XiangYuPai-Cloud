package com.xypai.user.domain.enums;

/**
 * 活动状态枚举
 *
 * @author XyPai
 * @since 2025-01-02
 */
public enum ActivityStatus {

    /**
     * 草稿 - 活动创建但未发布
     */
    DRAFT("草稿"),

    /**
     * 已发布 - 活动已发布，正在招募
     */
    PUBLISHED("已发布"),

    /**
     * 报名中 - 活动开始报名
     */
    REGISTERING("报名中"),

    /**
     * 报名截止 - 不再接受新的报名
     */
    REGISTRATION_CLOSED("报名截止"),

    /**
     * 进行中 - 活动正在进行
     */
    ONGOING("进行中"),

    /**
     * 已结束 - 活动已结束
     */
    FINISHED("已结束"),

    /**
     * 已取消 - 活动被取消
     */
    CANCELLED("已取消"),

    /**
     * 已删除 - 活动被删除（软删除）
     */
    DELETED("已删除");

    private final String description;

    ActivityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否可以编辑活动信息
     */
    public boolean isEditable() {
        return this == DRAFT || this == PUBLISHED || this == REGISTERING;
    }

    /**
     * 检查是否可以参与活动
     */
    public boolean isJoinable() {
        return this == PUBLISHED || this == REGISTERING;
    }

    /**
     * 检查是否可以取消活动
     */
    public boolean isCancellable() {
        return this == DRAFT || this == PUBLISHED || this == REGISTERING || this == REGISTRATION_CLOSED;
    }

    /**
     * 检查活动是否已经开始
     */
    public boolean isStarted() {
        return this == ONGOING || this == FINISHED;
    }

    /**
     * 检查活动是否结束
     */
    public boolean isEnded() {
        return this == FINISHED || this == CANCELLED || this == DELETED;
    }
}
