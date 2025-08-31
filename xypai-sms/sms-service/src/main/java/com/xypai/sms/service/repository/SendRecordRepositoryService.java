package com.xypai.sms.service.repository;

import com.xypai.sms.controller.dto.SmsSendRequest;
import com.xypai.sms.service.business.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repository: 短信发送记录仓储服务 (第一版：内存模拟实现)
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendRecordRepositoryService {

    private final ChannelService channelService;

    // 第一版：使用内存存储模拟数据库
    private final ConcurrentHashMap<String, SendRecord> recordStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Repository: 保存发送记录
     */
    public String saveSendRecord(SmsSendRequest request) {
        String taskId = generateTaskId();

        log.info("Repository: 保存发送记录, taskId={}, templateCode={}, phoneCount={}",
                taskId, request.templateCode(), request.phoneNumbers().size());

        SendRecord record = new SendRecord(
                idGenerator.getAndIncrement(),
                taskId,
                request.templateCode(),
                new java.util.ArrayList<>(request.phoneNumbers()),
                request.templateParams() != null ? new java.util.HashMap<>(request.templateParams()) : Map.of(),
                "PENDING",
                LocalDateTime.now(),
                null,
                channelService.selectChannel(null, null, request.phoneNumbers().iterator().next())
        );

        recordStore.put(taskId, record);

        log.info("Repository: 发送记录保存成功, taskId={}", taskId);
        return taskId;
    }

    /**
     * Repository: 更新发送状态
     */
    public void updateSendStatus(String taskId, String status) {
        log.info("Repository: 更新发送状态, taskId={}, status={}", taskId, status);

        SendRecord existingRecord = recordStore.get(taskId);
        if (existingRecord != null) {
            SendRecord updatedRecord = new SendRecord(
                    existingRecord.id(),
                    existingRecord.taskId(),
                    existingRecord.templateCode(),
                    existingRecord.phoneNumbers(),
                    existingRecord.templateParams(),
                    status,
                    existingRecord.createTime(),
                    LocalDateTime.now(), // 更新时间
                    existingRecord.channelType()
            );

            recordStore.put(taskId, updatedRecord);
            log.info("Repository: 发送状态更新成功, taskId={}, status={}", taskId, status);
        } else {
            log.warn("Repository: 发送记录不存在, taskId={}", taskId);
        }
    }

    /**
     * Repository: 根据任务ID查询发送记录
     */
    public SendRecord findByTaskId(String taskId) {
        log.debug("Repository: 查询发送记录, taskId={}", taskId);

        SendRecord record = recordStore.get(taskId);
        if (record != null) {
            log.debug("Repository: 找到发送记录, taskId={}", taskId);
        } else {
            log.warn("Repository: 发送记录不存在, taskId={}", taskId);
        }

        return record;
    }

    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return "SMS_" + System.currentTimeMillis() + "_" + idGenerator.get();
    }

    /**
     * 发送记录数据模型 (第一版：简单记录类)
     */
    public record SendRecord(
            Long id,
            String taskId,
            String templateCode,
            java.util.List<String> phoneNumbers,
            java.util.Map<String, Object> templateParams,
            String status,
            LocalDateTime createTime,
            LocalDateTime updateTime,
            String channelType
    ) {
    }
}