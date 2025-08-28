package com.xypai.user.domain.entity;

/**
 * ⚙️ 社交设置实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class SocialSettings {

    private boolean allowFollowNotification;     // 是否允许关注通知
    private boolean allowPrivateMessage;         // 是否允许私信
    private boolean showFollowingList;           // 是否公开关注列表
    private boolean showFollowerList;            // 是否公开粉丝列表
    private int privacyLevel;                    // 隐私级别 0-公开 1-好友 2-私密

    private SocialSettings(
            boolean allowFollowNotification,
            boolean allowPrivateMessage,
            boolean showFollowingList,
            boolean showFollowerList,
            int privacyLevel
    ) {
        this.allowFollowNotification = allowFollowNotification;
        this.allowPrivateMessage = allowPrivateMessage;
        this.showFollowingList = showFollowingList;
        this.showFollowerList = showFollowerList;
        this.privacyLevel = validatePrivacyLevel(privacyLevel);
    }

    /**
     * 创建默认社交设置
     */
    public static SocialSettings createDefault() {
        return new SocialSettings(
                true,   // 允许关注通知
                true,   // 允许私信
                true,   // 公开关注列表
                true,   // 公开粉丝列表
                0       // 公开级别
        );
    }

    /**
     * 从数据重建社交设置
     */
    public static SocialSettings fromData(
            boolean allowFollowNotification,
            boolean allowPrivateMessage,
            boolean showFollowingList,
            boolean showFollowerList,
            int privacyLevel
    ) {
        return new SocialSettings(
                allowFollowNotification,
                allowPrivateMessage,
                showFollowingList,
                showFollowerList,
                privacyLevel
        );
    }

    private static int validatePrivacyLevel(int privacyLevel) {
        if (privacyLevel < 0 || privacyLevel > 2) {
            throw new IllegalArgumentException("隐私级别只能是 0(公开)、1(好友) 或 2(私密)");
        }
        return privacyLevel;
    }

    /**
     * 更新隐私级别
     */
    public void updatePrivacyLevel(int privacyLevel) {
        this.privacyLevel = validatePrivacyLevel(privacyLevel);
    }

    public boolean isAllowFollowNotification() {
        return allowFollowNotification;
    }

    /**
     * 开启/关闭关注通知
     */
    public void setAllowFollowNotification(boolean allow) {
        this.allowFollowNotification = allow;
    }

    public boolean isAllowPrivateMessage() {
        return allowPrivateMessage;
    }

    /**
     * 开启/关闭私信
     */
    public void setAllowPrivateMessage(boolean allow) {
        this.allowPrivateMessage = allow;
    }

    // ========================================
    // Getters
    // ========================================

    public boolean isShowFollowingList() {
        return showFollowingList;
    }

    /**
     * 设置是否公开关注列表
     */
    public void setShowFollowingList(boolean show) {
        this.showFollowingList = show;
    }

    public boolean isShowFollowerList() {
        return showFollowerList;
    }

    /**
     * 设置是否公开粉丝列表
     */
    public void setShowFollowerList(boolean show) {
        this.showFollowerList = show;
    }

    public int getPrivacyLevel() {
        return privacyLevel;
    }

    @Override
    public String toString() {
        return String.format("SocialSettings{allowFollowNotification=%s, allowPrivateMessage=%s, " +
                        "showFollowingList=%s, showFollowerList=%s, privacyLevel=%d}",
                allowFollowNotification, allowPrivateMessage, showFollowingList,
                showFollowerList, privacyLevel);
    }
}
