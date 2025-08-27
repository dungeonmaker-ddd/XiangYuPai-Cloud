package com.xypai.user.domain.record;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户事件记录
 *
 * @author XyPai
 */
public record UserEvent(
        String eventType,
        Long userId,
        String username,
        String eventData,
        String operatorId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventTime
) {
    public UserEvent {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(userId, "用户ID不能为null");
        Objects.requireNonNull(username, "用户名不能为null");
        Objects.requireNonNull(eventTime, "事件时间不能为null");

        if (eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("事件类型不能为空字符串");
        }
        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空字符串");
        }
    }

    /**
     * 创建用户创建事件
     */
    public static UserEvent userCreated(Long userId, String username, String operatorId) {
        return new UserEvent(
                "USER_CREATED",
                userId,
                username,
                String.format("用户 %s 被创建", username),
                operatorId,
                LocalDateTime.now()
        );
    }

    /**
     * 创建用户更新事件
     */
    public static UserEvent userUpdated(Long userId, String username, String updateFields, String operatorId) {
        return new UserEvent(
                "USER_UPDATED",
                userId,
                username,
                String.format("用户 %s 被更新，更新字段: %s", username, updateFields),
                operatorId,
                LocalDateTime.now()
        );
    }

    /**
     * 创建用户删除事件
     */
    public static UserEvent userDeleted(Long userId, String username, String operatorId) {
        return new UserEvent(
                "USER_DELETED",
                userId,
                username,
                String.format("用户 %s 被删除", username),
                operatorId,
                LocalDateTime.now()
        );
    }

    /**
     * 创建用户状态变更事件
     */
    public static UserEvent userStatusChanged(Long userId, String username, Integer oldStatus,
                                              Integer newStatus, String operatorId) {
        return new UserEvent(
                "USER_STATUS_CHANGED",
                userId,
                username,
                String.format("用户 %s 状态从 %d 变更为 %d", username, oldStatus, newStatus),
                operatorId,
                LocalDateTime.now()
        );
    }

    /**
     * 创建用户登录事件
     */
    public static UserEvent userLogin(Long userId, String username, String ipAddress) {
        return new UserEvent(
                "USER_LOGIN",
                userId,
                username,
                String.format("用户 %s 从 %s 登录", username, ipAddress),
                userId.toString(),
                LocalDateTime.now()
        );
    }
}
