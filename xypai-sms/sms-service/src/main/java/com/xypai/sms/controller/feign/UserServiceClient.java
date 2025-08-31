package com.xypai.sms.controller.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign: 用户服务客户端
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@FeignClient(
        name = "user-service",
        path = "/api/v1/users"
)
public interface UserServiceClient {

    /**
     * Feign: 获取用户信息
     */
    @GetMapping("/{userId}")
    UserInfoResponse getUserInfo(@PathVariable Long userId);

    /**
     * User: 用户信息响应
     */
    record UserInfoResponse(
            Long id,
            String username,
            String email,
            String phone,
            Boolean active
    ) {
    }
}
