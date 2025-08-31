package com.xypai.sms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 📥 短信发送响应DTO
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Builder
@Schema(description = "短信发送响应数据传输对象")
public record SmsSendResponseDTO(

        @Schema(description = "请求ID", example = "req_123456789")
        String requestId,

        @Schema(description = "任务ID", example = "task_987654321")
        String taskId,

        @Schema(description = "发送状态", example = "SUCCESS", allowableValues = {"SUCCESS", "PARTIAL_SUCCESS", "FAILED", "PENDING"})
        String status,

        @Schema(description = "成功发送数量", example = "98")
        Integer successCount,

        @Schema(description = "失败发送数量", example = "2")
        Integer failedCount,

        @Schema(description = "总发送数量", example = "100")
        Integer totalCount,

        @Schema(description = "使用的渠道", example = "ALIYUN")
        String usedChannel,

        @Schema(description = "详细结果")
        List<SmsSendDetailDTO> details,

        @Schema(description = "错误信息", example = "部分号码在黑名单中")
        String errorMessage,

        @Schema(description = "发送时间", example = "2025-01-02T10:30:00")
        LocalDateTime sentAt
) {

    /**
     * 🏭 创建成功响应的工厂方法
     */
    public static SmsSendResponseDTO success(
            String requestId,
            String taskId,
            Integer totalCount,
            String usedChannel,
            List<SmsSendDetailDTO> details) {
        long successCount = details.stream().mapToLong(d -> d.isSuccess() ? 1 : 0).sum();
        return SmsSendResponseDTO.builder()
                .requestId(requestId)
                .taskId(taskId)
                .status("SUCCESS")
                .successCount((int) successCount)
                .failedCount(totalCount - (int) successCount)
                .totalCount(totalCount)
                .usedChannel(usedChannel)
                .details(details)
                .sentAt(LocalDateTime.now())
                .build();
    }

    /**
     * 🏭 创建失败响应的工厂方法
     */
    public static SmsSendResponseDTO failed(
            String requestId,
            String errorMessage) {
        return SmsSendResponseDTO.builder()
                .requestId(requestId)
                .status("FAILED")
                .successCount(0)
                .failedCount(0)
                .totalCount(0)
                .errorMessage(errorMessage)
                .sentAt(LocalDateTime.now())
                .build();
    }

    /**
     * ✅ 检查是否完全成功
     */
    public boolean isCompleteSuccess() {
        return "SUCCESS".equals(status) && failedCount == 0;
    }

    /**
     * ❌ 检查是否完全失败
     */
    public boolean isCompleteFailed() {
        return "FAILED".equals(status) || successCount == 0;
    }

    /**
     * 📊 获取成功率
     */
    public double getSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 📊 发送详情DTO
     */
    @Builder
    @Schema(description = "短信发送详情")
    public record SmsSendDetailDTO(

            @Schema(description = "手机号", example = "13800138000")
            String phoneNumber,

            @Schema(description = "发送状态", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED", "PENDING"})
            String status,

            @Schema(description = "渠道消息ID", example = "aliyun_msg_123456")
            String channelMessageId,

            @Schema(description = "错误代码", example = "BLACKLIST")
            String errorCode,

            @Schema(description = "错误信息", example = "手机号在黑名单中")
            String errorMessage
    ) {

        /**
         * ✅ 检查是否发送成功
         */
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
    }
}
