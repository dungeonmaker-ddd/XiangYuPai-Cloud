package com.xypai.sms.controller.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO: 短信状态响应
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public record SmsStatusResponse(
        String taskId,
        String status,
        Integer totalCount,
        Integer successCount,
        Integer failedCount,
        String usedChannel,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {

    /**
     * DTO: 紧凑构造器验证
     */
    public SmsStatusResponse {
        Objects.requireNonNull(taskId, "任务ID不能为空");
        Objects.requireNonNull(status, "状态不能为空");

        if (totalCount == null) totalCount = 0;
        if (successCount == null) successCount = 0;
        if (failedCount == null) failedCount = 0;
    }

    /**
     * DTO: 创建响应
     */
    public static SmsStatusResponse of(
            String taskId,
            String status,
            Integer totalCount,
            Integer successCount,
            Integer failedCount) {
        return new SmsStatusResponse(
                taskId,
                status,
                totalCount,
                successCount,
                failedCount,
                null,
                null,
                null
        );
    }

    /**
     * DTO: 计算成功率
     */
    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100.0;
    }

    /**
     * DTO: 检查是否完成
     */
    public boolean isCompleted() {
        return "SUCCESS".equals(status) || "FAILED".equals(status) || "PARTIAL_SUCCESS".equals(status);
    }
}
