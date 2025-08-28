package com.xypai.user.domain.entity;

import com.xypai.user.domain.enums.ActivityType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 活动信息实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record ActivityInfo(
        String title,
        String description,
        ActivityType type,
        String coverImageUrl,
        List<String> tags,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        String onlineLink,
        int maxParticipants,
        boolean isPublic,
        boolean requiresApproval,
        String requirements,
        String contactInfo
) {

    public ActivityInfo {
        Objects.requireNonNull(title, "活动标题不能为空");
        Objects.requireNonNull(description, "活动描述不能为空");
        Objects.requireNonNull(type, "活动类型不能为空");
        Objects.requireNonNull(startTime, "开始时间不能为空");
        Objects.requireNonNull(endTime, "结束时间不能为空");

        // 业务规则验证
        validateTitle(title);
        validateDescription(description);
        validateTimeRange(startTime, endTime);
        validateParticipantLimit(maxParticipants);
        validateLocationRequirements(type, location, onlineLink);

        // 确保tags不为null
        if (tags == null) {
            tags = List.of();
        }
    }

    /**
     * 静态工厂方法：创建活动信息
     */
    public static ActivityInfo create(
            String title,
            String description,
            ActivityType type,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String location,
            int maxParticipants
    ) {
        return new ActivityInfo(
                title,
                description,
                type,
                null, // coverImageUrl
                List.of(), // tags
                startTime,
                endTime,
                location,
                null, // onlineLink
                maxParticipants,
                true, // isPublic
                false, // requiresApproval
                null, // requirements
                null // contactInfo
        );
    }

    private static void validateTitle(String title) {
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("活动标题不能为空");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("活动标题长度不能超过100个字符");
        }
    }

    private static void validateDescription(String description) {
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("活动描述不能为空");
        }
        if (description.length() > 2000) {
            throw new IllegalArgumentException("活动描述长度不能超过2000个字符");
        }
    }

    private static void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        if (startTime.isBefore(LocalDateTime.now().minusMinutes(30))) {
            throw new IllegalArgumentException("开始时间不能早于当前时间30分钟");
        }

        long durationHours = java.time.Duration.between(startTime, endTime).toHours();
        if (durationHours > 168) { // 7天
            throw new IllegalArgumentException("活动持续时间不能超过7天");
        }
    }

    private static void validateParticipantLimit(int maxParticipants) {
        if (maxParticipants < 1) {
            throw new IllegalArgumentException("最大参与人数必须大于0");
        }
        if (maxParticipants > 10000) {
            throw new IllegalArgumentException("最大参与人数不能超过10000");
        }
    }

    private static void validateLocationRequirements(ActivityType type, String location, String onlineLink) {
        if (type.requiresLocation() && (location == null || location.trim().isEmpty())) {
            throw new IllegalArgumentException("线下活动必须提供位置信息");
        }

        if (type.isOnline() && (onlineLink == null || onlineLink.trim().isEmpty())) {
            throw new IllegalArgumentException("线上活动必须提供在线链接");
        }
    }

    // ========================================
    // 私有验证方法
    // ========================================

    /**
     * 更新活动信息
     */
    public ActivityInfo update(
            String newTitle,
            String newDescription,
            LocalDateTime newStartTime,
            LocalDateTime newEndTime,
            String newLocation,
            int newMaxParticipants
    ) {
        return new ActivityInfo(
                newTitle != null ? newTitle : this.title,
                newDescription != null ? newDescription : this.description,
                this.type,
                this.coverImageUrl,
                this.tags,
                newStartTime != null ? newStartTime : this.startTime,
                newEndTime != null ? newEndTime : this.endTime,
                newLocation != null ? newLocation : this.location,
                this.onlineLink,
                newMaxParticipants > 0 ? newMaxParticipants : this.maxParticipants,
                this.isPublic,
                this.requiresApproval,
                this.requirements,
                this.contactInfo
        );
    }

    /**
     * 添加标签
     */
    public ActivityInfo addTag(String tag) {
        Objects.requireNonNull(tag, "标签不能为空");
        if (tags.contains(tag)) {
            return this;
        }

        var newTags = new java.util.ArrayList<>(tags);
        newTags.add(tag);

        return new ActivityInfo(
                this.title,
                this.description,
                this.type,
                this.coverImageUrl,
                List.copyOf(newTags),
                this.startTime,
                this.endTime,
                this.location,
                this.onlineLink,
                this.maxParticipants,
                this.isPublic,
                this.requiresApproval,
                this.requirements,
                this.contactInfo
        );
    }

    /**
     * 检查活动是否已经开始
     */
    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(startTime);
    }

    /**
     * 检查活动是否已经结束
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * 获取活动持续时间（小时）
     */
    public long getDurationHours() {
        return java.time.Duration.between(startTime, endTime).toHours();
    }
}
