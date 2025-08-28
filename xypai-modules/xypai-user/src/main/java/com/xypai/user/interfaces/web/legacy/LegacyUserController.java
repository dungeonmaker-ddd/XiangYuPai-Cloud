package com.xypai.user.interfaces.web.legacy;

import com.xypai.user.interfaces.dto.request.UserRegisterRequest;
import com.xypai.user.interfaces.dto.request.UserUpdateRequest;
import com.xypai.user.interfaces.dto.response.DetailedUserResponse;
import com.xypai.user.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 传统用户管理接口 (待重构)
 *
 * @author XyPai
 * @deprecated 使用新的DDD风格UserController代替
 */
@RestController
@RequestMapping("/legacy/users")
@Tag(name = "传统用户管理", description = "传统MVC风格用户管理接口(待重构)")
@Deprecated
public class LegacyUserController {

    @Autowired
    private AppUserService appUserService;

    @Operation(summary = "用户注册", description = "APP用户注册接口，支持手机号注册")
    @PostMapping("/register")
    public DetailedUserResponse register(@Valid @RequestBody UserRegisterRequest request) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.register(request);
    }

    @Operation(summary = "根据手机号获取用户信息", description = "通过手机号查询用户详细信息")
    @GetMapping("/profile/{mobile}")
    public DetailedUserResponse getProfileByMobile(
            @Parameter(description = "手机号", example = "13888888888")
            @PathVariable("mobile")
            @NotBlank(message = "手机号不能为空")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            String mobile) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.getByMobile(mobile);
    }

    @Operation(summary = "根据用户ID获取用户信息", description = "通过用户ID查询用户详细信息")
    @GetMapping("/profile/id/{id}")
    public DetailedUserResponse getProfileById(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.getById(id);
    }

    @Operation(summary = "更新用户信息", description = "更新APP用户个人信息")
    @PutMapping("/profile")
    public DetailedUserResponse updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.updateProfile(request);
    }

    @Operation(summary = "禁用用户", description = "将用户状态设置为禁用")
    @PutMapping("/{id}/disable")
    public Boolean disableUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.updateStatus(id, 0);
    }

    @Operation(summary = "启用用户", description = "将用户状态设置为正常")
    @PutMapping("/{id}/enable")
    public Boolean enableUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.updateStatus(id, 1);
    }

    @Operation(summary = "更新最后登录时间", description = "用户登录时更新最后登录时间")
    @PutMapping("/{id}/last-login")
    public Boolean updateLastLoginTime(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.updateLastLoginTime(id);
    }

    @Operation(summary = "检查手机号是否已注册", description = "检查手机号是否已被注册")
    @GetMapping("/check/mobile")
    public Boolean checkMobile(
            @Parameter(description = "手机号", example = "13888888888")
            @RequestParam("mobile")
            @NotBlank(message = "手机号不能为空")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            String mobile) {
        // TODO: 转换为新的DDD风格调用
        return null; // !appUserService.existsByMobile(mobile);
    }

    @Operation(summary = "检查用户名是否已存在", description = "检查用户名是否已被使用")
    @GetMapping("/check/username")
    public Boolean checkUsername(
            @Parameter(description = "用户名", example = "testuser")
            @RequestParam("username") String username,
            @Parameter(description = "排除的用户ID，更新时使用")
            @RequestParam(value = "excludeId", required = false) Long excludeId) {
        // TODO: 转换为新的DDD风格调用
        return null; // !appUserService.existsByUsername(username, excludeId);
    }

    // ========================================
    // 软删除相关接口
    // ========================================

    @Operation(summary = "软删除用户", description = "将用户标记为已删除，可恢复")
    @DeleteMapping("/{id}")
    public Boolean softDeleteUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.softDeleteUser(id);
    }

    @Operation(summary = "恢复已删除用户", description = "恢复之前被软删除的用户")
    @PutMapping("/{id}/restore")
    public Boolean restoreUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.restoreUser(id);
    }

    @Operation(summary = "查询已删除用户", description = "获取已删除用户信息")
    @GetMapping("/deleted/{id}")
    public DetailedUserResponse getDeletedUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.getDeletedById(id);
    }

    @Operation(summary = "获取已删除用户列表", description = "分页查询已删除用户列表")
    @GetMapping("/deleted")
    public List<DetailedUserResponse> getDeletedUsers(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.getDeletedUsers(pageNum, pageSize);
    }

    @Operation(summary = "统计已删除用户数量", description = "获取已删除用户总数")
    @GetMapping("/deleted/count")
    public Long getDeletedCount() {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.getDeletedCount();
    }

    @Operation(summary = "物理删除用户", description = "永久删除用户数据，不可恢复（危险操作）")
    @DeleteMapping("/{id}/physical")
    public Boolean physicalDeleteUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        // TODO: 转换为新的DDD风格调用
        return null; // appUserService.physicalDeleteUser(id);
    }
}
