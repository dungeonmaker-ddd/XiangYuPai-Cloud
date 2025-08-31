package com.xypai.sms.service.business;

import com.xypai.sms.common.exception.BusinessException;
import com.xypai.sms.controller.dto.SmsSendRequest;
import com.xypai.sms.controller.dto.SmsSendResponse;
import com.xypai.sms.controller.dto.SmsStatusResponse;
import com.xypai.sms.service.repository.SendRecordRepositoryService;
import com.xypai.sms.service.repository.SmsTemplateRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Business: 短信发送业务服务
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsTemplateRepositoryService templateRepository;
    private final SendRecordRepositoryService sendRecordRepository;
    private final ChannelService channelService;
    private final ValidationService validationService;

    /**
     * Business: 发送短信
     */
    public SmsSendResponse sendSms(SmsSendRequest request) {
        log.info("Business: 开始发送短信, templateCode={}, phoneCount={}",
                request.templateCode(), request.getPhoneCount());

        try {
            // 1. 验证请求参数
            validationService.validateSendRequest(request);

            // 2. 验证模板
            var template = templateRepository.findByTemplateCode(request.templateCode());
            if (template == null) {
                throw new BusinessException.TemplateNotFoundException(request.templateCode());
            }
            if (!"ACTIVE".equals(template.status())) {
                throw new BusinessException("TEMPLATE_INACTIVE", "模板未激活: " + request.templateCode());
            }

            // 3. 验证手机号
            Set<String> validPhones = validationService.validatePhoneNumbers(request.phoneNumbers());

            // 4. 选择发送渠道
            String selectedChannel = channelService.selectChannel(
                    request.preferredChannel(),
                    request.loadBalanceStrategy(),
                    validPhones.iterator().next()
            );

            // 5. 保存发送记录
            String taskId = sendRecordRepository.saveSendRecord(request);

            if (request.isAsync()) {
                // 异步发送
                return sendAsync(taskId, request, template, validPhones, selectedChannel);
            } else {
                // 同步发送
                return sendSync(taskId, request, template, validPhones, selectedChannel);
            }

        } catch (BusinessException e) {
            log.error("Business: 短信发送业务异常, templateCode={}, error={}",
                    request.templateCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Business: 短信发送系统异常, templateCode={}, error={}",
                    request.templateCode(), e.getMessage(), e);
            throw new BusinessException.SmsSendException("短信发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * Business: 查询发送状态
     */
    public SmsStatusResponse getSendStatus(String taskId) {
        log.debug("Business: 查询发送状态, taskId={}", taskId);

        var sendRecord = sendRecordRepository.findByTaskId(taskId);
        if (sendRecord == null) {
            throw new BusinessException("TASK_NOT_FOUND", "发送任务不存在: " + taskId);
        }

        return SmsStatusResponse.of(
                sendRecord.taskId(),
                sendRecord.status(),
                sendRecord.phoneNumbers().size(),
                sendRecord.status().equals("SUCCESS") ? sendRecord.phoneNumbers().size() : 0,
                sendRecord.status().equals("FAILED") ? sendRecord.phoneNumbers().size() : 0
        );
    }

    /**
     * Business: 同步发送
     */
    private SmsSendResponse sendSync(
            String taskId,
            SmsSendRequest request,
            Object template,
            Set<String> phones,
            String channel) {

        log.info("Business: 同步发送短信, taskId={}, channel={}, phoneCount={}",
                taskId, channel, phones.size());

        // 第一版：模拟发送逻辑
        try {
            // 模拟发送延迟
            Thread.sleep(100);

            // 更新发送状态为成功
            sendRecordRepository.updateSendStatus(taskId, "SUCCESS");

            log.info("Business: 模拟发送成功, taskId={}, successCount={}", taskId, phones.size());

            return SmsSendResponse.success(
                    request.requestId(),
                    taskId,
                    phones.size(),
                    phones.size(),
                    channel
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Business: 同步发送失败, taskId={}, error={}", taskId, e.getMessage(), e);

            // 更新发送状态为失败
            sendRecordRepository.updateSendStatus(taskId, "FAILED");

            return SmsSendResponse.failed(request.requestId(), e.getMessage());
        }
    }

    /**
     * Business: 异步发送
     */
    private SmsSendResponse sendAsync(
            String taskId,
            SmsSendRequest request,
            Object template,
            Set<String> phones,
            String channel) {

        log.info("Business: 异步发送短信, taskId={}, channel={}, phoneCount={}",
                taskId, channel, phones.size());

        // 第一版：模拟异步处理 - 立即返回，后台处理
        // 在实际项目中这里会发送到消息队列
        sendRecordRepository.updateSendStatus(taskId, "PROCESSING");

        // 模拟异步处理：启动后台线程
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(5000); // 模拟处理时间
                sendRecordRepository.updateSendStatus(taskId, "SUCCESS");
                log.info("Business: 异步发送完成, taskId={}", taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sendRecordRepository.updateSendStatus(taskId, "FAILED");
                log.error("Business: 异步发送失败, taskId={}", taskId);
            }
        });

        // 立即返回响应
        return SmsSendResponse.success(
                request.requestId(),
                taskId,
                phones.size(),
                0, // 异步发送时立即返回，成功数为0
                channel
        );
    }


}
