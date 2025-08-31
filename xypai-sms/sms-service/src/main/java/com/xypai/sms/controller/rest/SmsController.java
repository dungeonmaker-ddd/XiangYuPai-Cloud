package com.xypai.sms.controller.rest;

import com.xypai.sms.controller.dto.SmsSendRequest;
import com.xypai.sms.controller.dto.SmsSendResponse;
import com.xypai.sms.controller.dto.SmsStatusResponse;
import com.xypai.sms.service.business.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SMS: 短信发送控制器
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
@Tag(name = "短信服务", description = "短信发送相关API")
public class SmsController {

    private final SmsService smsService;

    /**
     * Controller: 发送短信
     */
    @Operation(summary = "发送短信", description = "支持单个或批量发送短信")
    @PostMapping("/send")
    public ResponseEntity<SmsSendResponse> sendSms(
            @Valid @RequestBody SmsSendRequest request) {

        log.info("Controller: 接收短信发送请求, templateCode={}, phoneCount={}",
                request.templateCode(), request.phoneNumbers().size());

        SmsSendResponse response = smsService.sendSms(request);

        log.info("Controller: 短信发送完成, taskId={}, status={}",
                response.taskId(), response.status());

        return ResponseEntity.ok(response);
    }

    /**
     * Controller: 查询发送状态
     */
    @Operation(summary = "查询发送状态", description = "根据任务ID查询短信发送状态")
    @GetMapping("/send/{taskId}/status")
    public ResponseEntity<SmsStatusResponse> getSendStatus(
            @Parameter(description = "任务ID", required = true)
            @PathVariable String taskId) {

        log.debug("Controller: 查询发送状态, taskId={}", taskId);

        SmsStatusResponse response = smsService.getSendStatus(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Controller: 获取发送统计
     */
    @Operation(summary = "获取发送统计", description = "获取指定时间范围内的短信发送统计")
    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics(
            @Parameter(description = "开始日期", example = "2025-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期", example = "2025-01-31")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "渠道类型", example = "ALIYUN")
            @RequestParam(required = false) String channelType) {

        log.info("Controller: 查询发送统计, startDate={}, endDate={}, channelType={}",
                startDate, endDate, channelType);

        // TODO: 实现统计查询逻辑
        return ResponseEntity.ok("统计功能开发中...");
    }
}
