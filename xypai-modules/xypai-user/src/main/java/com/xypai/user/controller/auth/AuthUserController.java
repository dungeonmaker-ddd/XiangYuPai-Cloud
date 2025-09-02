package com.xypai.user.controller.auth;

import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.security.annotation.InnerAuth;
import com.xypai.user.domain.dto.AuthUserQueryDTO;
import com.xypai.user.domain.dto.AutoRegisterDTO;
import com.xypai.user.domain.dto.UserValidateDTO;
import com.xypai.user.domain.vo.AuthUserVO;
import com.xypai.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证用户控制器(内部调用)
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "认证用户管理", description = "供认证服务内部调用的用户API")
@RestController
@RequestMapping("/api/v1/users/auth")
@RequiredArgsConstructor
public class AuthUserController extends BaseController {

    private final IUserService userService;

    /**
     * 根据用户名获取用户信息(认证服务专用)
     */
    @Operation(summary = "根据用户名获取用户信息", description = "认证服务专用接口")
    @GetMapping("/username/{username}")
    @InnerAuth
    public R<AuthUserVO> getUserByUsername(@PathVariable("username") String username) {
        AuthUserVO userVO = userService.selectAuthUserByUsername(username);
        return userVO != null ? R.ok(userVO) : R.fail("用户不存在");
    }

    /**
     * 根据手机号获取用户信息(认证服务专用)
     */
    @Operation(summary = "根据手机号获取用户信息", description = "认证服务专用接口")
    @GetMapping("/mobile/{mobile}")
    @InnerAuth
    public R<AuthUserVO> getUserByMobile(@PathVariable("mobile") String mobile) {
        AuthUserVO userVO = userService.selectAuthUserByMobile(mobile);
        return userVO != null ? R.ok(userVO) : R.fail("用户不存在");
    }

    /**
     * 验证用户密码(认证服务专用)
     */
    @Operation(summary = "验证用户密码", description = "认证服务专用接口")
    @PostMapping("/validate-password")
    @InnerAuth
    public R<Boolean> validatePassword(@RequestBody UserValidateDTO validateDTO) {
        boolean valid = userService.validateUserPassword(validateDTO);
        return R.ok(valid);
    }

    /**
     * 更新用户最后登录时间(认证服务专用)
     */
    @Operation(summary = "更新用户最后登录时间", description = "认证服务专用接口")
    @PostMapping("/update-login-time/{userId}")
    @InnerAuth
    public R<Void> updateLastLoginTime(@PathVariable("userId") Long userId) {
        boolean success = userService.updateLastLoginTime(userId);
        return success ? R.ok() : R.fail("更新失败");
    }

    /**
     * 短信登录时自动注册用户(认证服务专用)
     */
    @Operation(summary = "短信登录自动注册", description = "认证服务专用接口，短信验证成功后自动创建用户")
    @PostMapping("/auto-register")
    @InnerAuth
    public R<AuthUserVO> autoRegisterUser(@RequestBody AutoRegisterDTO autoRegisterDTO) {
        AuthUserVO userVO = userService.autoRegisterUser(autoRegisterDTO);
        return userVO != null ? R.ok(userVO) : R.fail("自动注册失败");
    }
}
