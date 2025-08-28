package com.xypai.user.domain.activity.enums;

/**
 * 活动类型枚举
 *
 * @author XyPai
 * @since 2025-01-02
 */
public enum ActivityType {

    /**
     * 线下聚会
     */
    OFFLINE_MEETUP("线下聚会", true),

    /**
     * 线上活动
     */
    ONLINE_EVENT("线上活动", false),

    /**
     * 运动健身
     */
    SPORTS_FITNESS("运动健身", true),

    /**
     * 学习分享
     */
    LEARNING_SHARING("学习分享", false),

    /**
     * 旅行出游
     */
    TRAVEL_TOUR("旅行出游", true),

    /**
     * 美食探店
     */
    FOOD_EXPLORATION("美食探店", true),

    /**
     * 艺术文化
     */
    ART_CULTURE("艺术文化", true),

    /**
     * 公益活动
     */
    CHARITY_EVENT("公益活动", true),

    /**
     * 商务交流
     */
    BUSINESS_NETWORKING("商务交流", true),

    /**
     * 游戏竞技
     */
    GAMING_COMPETITION("游戏竞技", false),

    /**
     * 兴趣爱好
     */
    HOBBY_INTEREST("兴趣爱好", true),

    /**
     * 其他活动
     */
    OTHER("其他活动", true);

    private final String description;
    private final boolean requiresLocation;

    ActivityType(String description, boolean requiresLocation) {
        this.description = description;
        this.requiresLocation = requiresLocation;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 是否需要位置信息
     */
    public boolean requiresLocation() {
        return requiresLocation;
    }

    /**
     * 是否为线下活动
     */
    public boolean isOffline() {
        return requiresLocation;
    }

    /**
     * 是否为线上活动
     */
    public boolean isOnline() {
        return !requiresLocation;
    }
}
