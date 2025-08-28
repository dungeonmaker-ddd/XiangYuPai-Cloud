package com.xypai.auth.app.auth.service;

import com.xypai.user.domain.record.AppUserRegisterRequest;
import com.xypai.user.domain.record.AppUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 远程APP用户服务调用
 *
 * @author XyPai
 */
@FeignClient(contextId = "authAppUserService", value = "xypai-user", fallback = RemoteAppUserFallback.class)
public interface RemoteAppUserService {

    /**
     * 用户注册
     */
    @PostMapping("/users/register")
    AppUserResponse register(@RequestBody AppUserRegisterRequest request);

    /**
     * 根据手机号获取用户信息
     */
    @GetMapping("/users/profile/{mobile}")
    Optional<AppUserResponse> getByMobile(@PathVariable("mobile") String mobile);

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/users/profile/id/{id}")
    Optional<AppUserResponse> getById(@PathVariable("id") Long id);

    /**
     * 更新最后登录时间
     */
    @PutMapping("/users/{id}/last-login")
    Boolean updateLastLoginTime(@PathVariable("id") Long id);

    /**
     * 检查手机号是否已注册
     */
    @GetMapping("/users/check/mobile")
    Boolean checkMobile(@RequestParam("mobile") String mobile);
}
