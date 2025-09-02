package com.xypai.auth.feign;

import com.xypai.common.core.domain.R;
import com.xypai.auth.feign.dto.AuthUserDTO;
import com.xypai.auth.feign.dto.AutoRegisterDTO;
import com.xypai.auth.feign.dto.UserValidateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务Feign客户端
 *
 * @author xypai
 * @date 2025-01-01
 */
@FeignClient(
        name = "xypai-user",
        path = "/api/v1/users"
)
public interface UserServiceFeign {

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/auth/username/{username}")
    R<AuthUserDTO> getUserByUsername(@PathVariable("username") String username);

    /**
     * 根据手机号获取用户信息
     */
    @GetMapping("/auth/mobile/{mobile}")
    R<AuthUserDTO> getUserByMobile(@PathVariable("mobile") String mobile);

    /**
     * 验证用户密码
     */
    @PostMapping("/auth/validate-password")
    R<Boolean> validatePassword(@RequestBody UserValidateDTO validateDTO);

    /**
     * 更新用户最后登录时间
     */
    @PostMapping("/auth/update-login-time/{userId}")
    R<Void> updateLastLoginTime(@PathVariable("userId") Long userId);

    /**
     * 短信登录自动注册用户
     */
    @PostMapping("/auth/auto-register")
    R<AuthUserDTO> autoRegisterUser(@RequestBody AutoRegisterDTO autoRegisterDTO);
}
