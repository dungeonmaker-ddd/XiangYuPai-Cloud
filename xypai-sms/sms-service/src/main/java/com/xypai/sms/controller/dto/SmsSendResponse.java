package com.xypai.sms.controller.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * DTO: 短信发送响应
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public record SmsSendResponse(
        String requestId,
        String taskId,
        String status,
        Integer totalCount,
        Integer successCount,
        Integer failedCount,
        String usedChannel,
        String errorMessage,
        LocalDateTime sentAt,
        List<SmsSendDetail> details
) {

    /**
     * DTO: 紧凑构造器验证
     */
    public SmsSendResponse {
        Objects.requireNonNull(taskId, "任务ID不能为空");
        Objects.requireNonNull(status, "状态不能为空");

        if (totalCount == null) totalCount = 0;
        if (successCount == null) successCount = 0;
        if (failedCount == null) failedCount = 0;
        if (sentAt == null) sentAt = LocalDateTime.now();
        if (details == null) details = List.of();
    }

    /**
     * DTO: 创建成功响应
     */
    public static SmsSendResponse success(
            String requestId,
            String taskId,
            Integer totalCount,
            Integer successCount,
            String usedChannel) {
        return new SmsSendResponse(
                requestId,
                taskId,
                "SUCCESS",
                totalCount,
                successCount,
                0,
                usedChannel,
                null,
                LocalDateTime.now(),
                List.of()
        );
    }

    /**
     * DTO: 创建失败响应
     */
    public static SmsSendResponse failed(String requestId, String errorMessage) {
        return new SmsSendResponse(
                requestId,
                "task_" + System.currentTimeMillis(),
                "FAILED",
                0,
                0,
                0,
                null,
                errorMessage,
                LocalDateTime.now(),
                List.of()
        );
    }

    /**
     * DTO: 短信发送详情
     */
    public record SmsSendDetail(
            String phoneNumber,
            String status,
            String channelMessageId,
            String errorCode,
            String errorMessage,
            LocalDateTime sentAt
    ) {

        public SmsSendDetail {
            Objects.requireNonNull(phoneNumber, "手机号不能为空");
            Objects.requireNonNull(status, "状态不能为空");

            if (sentAt == null) {
                sentAt = LocalDateTime.now();
            }
        }

        /**
         * DTO: 创建成功详情
         */
        public static SmsSendDetail success(String phoneNumber, String channelMessageId) {
            return new SmsSendDetail(
                    phoneNumber,
                    "SUCCESS",
                    channelMessageId,
                    null,
                    null,
                    LocalDateTime.now()
            );
        }

        /**
         * DTO: 创建失败详情
         */
        public static SmsSendDetail failed(String phoneNumber, String errorCode, String errorMessage) {
            return new SmsSendDetail(
                    phoneNumber,
                    "FAILED",
                    null,
                    errorCode,
                    errorMessage,
                    LocalDateTime.now()
            );
        }
    }
}
