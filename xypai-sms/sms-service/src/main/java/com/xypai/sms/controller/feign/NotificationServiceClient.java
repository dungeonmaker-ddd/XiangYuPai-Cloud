package com.xypai.sms.controller.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign: 通知服务客户端
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@FeignClient(
        name = "notification-service",
        path = "/api/v1/notifications"
)
public interface NotificationServiceClient {

    /**
     * Feign: 发送通知
     */
    @PostMapping("/send")
    NotificationResponse sendNotification(@RequestBody NotificationRequest request);

    /**
     * Notification: 通知请求
     */
    record NotificationRequest(
            String type,
            String title,
            String content,
            String target
    ) {
    }

    /**
     * Notification: 通知响应
     */
    record NotificationResponse(
            String id,
            String status,
            String message
    ) {
    }
}
