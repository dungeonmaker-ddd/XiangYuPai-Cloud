package com.xypai.user.domain.entity;


/**
 * 活动设置实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record ActivitySettings(
        boolean isPublic,              // 是否公开
        boolean requiresApproval,      // 是否需要审批
        boolean allowComments,         // 是否允许评论
        boolean allowSharing,          // 是否允许分享
        boolean enableReminder,        // 是否开启提醒
        int reminderHours,            // 提醒提前小时数
        boolean enableCheckIn,         // 是否开启签到
        boolean allowCancellation,     // 是否允许取消参与
        int cancellationDeadlineHours, // 取消参与截止时间（小时）
        String joinMessage,           // 参与成功消息
        String cancellationMessage,   // 取消参与消息
        boolean enableWaitingList,    // 是否开启候补列表
        int maxWaitingList           // 最大候补人数
) {

    public ActivitySettings {
        // 验证提醒小时数
        if (enableReminder && (reminderHours < 1 || reminderHours > 168)) {
            throw new IllegalArgumentException("提醒时间必须在1-168小时之间");
        }

        // 验证取消截止时间
        if (allowCancellation && cancellationDeadlineHours < 0) {
            throw new IllegalArgumentException("取消截止时间不能为负数");
        }

        // 验证候补列表设置
        if (enableWaitingList && maxWaitingList < 0) {
            throw new IllegalArgumentException("候补列表人数不能为负数");
        }
        if (enableWaitingList && maxWaitingList > 1000) {
            throw new IllegalArgumentException("候补列表人数不能超过1000");
        }

        // 验证消息长度
        if (joinMessage != null && joinMessage.length() > 200) {
            throw new IllegalArgumentException("参与成功消息不能超过200个字符");
        }
        if (cancellationMessage != null && cancellationMessage.length() > 200) {
            throw new IllegalArgumentException("取消参与消息不能超过200个字符");
        }
    }

    /**
     * 创建默认设置
     */
    public static ActivitySettings defaultSettings() {
        return new ActivitySettings(
                true,  // isPublic
                false, // requiresApproval
                true,  // allowComments
                true,  // allowSharing
                true,  // enableReminder
                24,    // reminderHours (提前24小时提醒)
                true,  // enableCheckIn
                true,  // allowCancellation
                24,    // cancellationDeadlineHours (提前24小时截止取消)
                "恭喜你成功参与活动！期待与你相见。", // joinMessage
                "很遗憾你取消了参与，期待下次活动再见。", // cancellationMessage
                true,  // enableWaitingList
                50     // maxWaitingList
        );
    }

    /**
     * 创建公开活动设置
     */
    public static ActivitySettings publicSettings() {
        return new ActivitySettings(
                true,  // isPublic
                false, // requiresApproval
                true,  // allowComments
                true,  // allowSharing
                true,  // enableReminder
                24,    // reminderHours
                true,  // enableCheckIn
                true,  // allowCancellation
                12,    // cancellationDeadlineHours (提前12小时截止取消)
                "欢迎参与我们的活动！", // joinMessage
                "取消参与成功。",      // cancellationMessage
                true,  // enableWaitingList
                100    // maxWaitingList
        );
    }

    /**
     * 创建私密活动设置
     */
    public static ActivitySettings privateSettings() {
        return new ActivitySettings(
                false, // isPublic
                true,  // requiresApproval
                true,  // allowComments
                false, // allowSharing
                true,  // enableReminder
                48,    // reminderHours (提前48小时提醒)
                true,  // enableCheckIn
                true,  // allowCancellation
                48,    // cancellationDeadlineHours (提前48小时截止取消)
                "你的申请已通过，欢迎参与私密活动！", // joinMessage
                "取消参与成功。",                 // cancellationMessage
                false, // enableWaitingList
                0      // maxWaitingList
        );
    }

    /**
     * 更新公开性设置
     */
    public ActivitySettings withPublicSetting(boolean isPublic) {
        return new ActivitySettings(
                isPublic,
                this.requiresApproval,
                this.allowComments,
                this.allowSharing,
                this.enableReminder,
                this.reminderHours,
                this.enableCheckIn,
                this.allowCancellation,
                this.cancellationDeadlineHours,
                this.joinMessage,
                this.cancellationMessage,
                this.enableWaitingList,
                this.maxWaitingList
        );
    }

    /**
     * 更新审批设置
     */
    public ActivitySettings withApprovalSetting(boolean requiresApproval) {
        return new ActivitySettings(
                this.isPublic,
                requiresApproval,
                this.allowComments,
                this.allowSharing,
                this.enableReminder,
                this.reminderHours,
                this.enableCheckIn,
                this.allowCancellation,
                this.cancellationDeadlineHours,
                this.joinMessage,
                this.cancellationMessage,
                this.enableWaitingList,
                this.maxWaitingList
        );
    }

    /**
     * 更新提醒设置
     */
    public ActivitySettings withReminderSetting(boolean enableReminder, int reminderHours) {
        return new ActivitySettings(
                this.isPublic,
                this.requiresApproval,
                this.allowComments,
                this.allowSharing,
                enableReminder,
                reminderHours,
                this.enableCheckIn,
                this.allowCancellation,
                this.cancellationDeadlineHours,
                this.joinMessage,
                this.cancellationMessage,
                this.enableWaitingList,
                this.maxWaitingList
        );
    }

    /**
     * 检查是否允许当前时间取消参与
     */
    public boolean canCancelNow(java.time.LocalDateTime activityStartTime) {
        if (!allowCancellation) {
            return false;
        }

        java.time.LocalDateTime deadline = activityStartTime.minusHours(cancellationDeadlineHours);
        return java.time.LocalDateTime.now().isBefore(deadline);
    }

    /**
     * 检查是否需要发送提醒
     */
    public boolean shouldSendReminder(java.time.LocalDateTime activityStartTime) {
        if (!enableReminder) {
            return false;
        }

        java.time.LocalDateTime reminderTime = activityStartTime.minusHours(reminderHours);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 在提醒时间前后30分钟内都可以发送提醒
        return now.isAfter(reminderTime.minusMinutes(30)) &&
                now.isBefore(reminderTime.plusMinutes(30));
    }
}
