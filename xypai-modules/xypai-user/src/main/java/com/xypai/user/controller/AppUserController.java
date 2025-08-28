package com.xypai.user.controller;

import com.xypai.user.domain.record.AppUserRegisterRequest;
import com.xypai.user.domain.record.AppUserResponse;
import com.xypai.user.domain.record.AppUserUpdateRequest;
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
import java.util.Optional;

/**
 * APP用户管理接口
 *
 * @author XyPai
 */
@RestController
@RequestMapping("/users")
@Tag(name = "APP用户管理", description = "APP端用户注册、信息管理等接口")
public class AppUserController {

    @Autowired
    private AppUserService appUserService;

    @Operation(summary = "用户注册", description = "APP用户注册接口，支持手机号注册")
    @PostMapping("/register")
    public AppUserResponse register(@Valid @RequestBody AppUserRegisterRequest request) {
        return appUserService.register(request);
    }

    @Operation(summary = "根据手机号获取用户信息", description = "通过手机号查询用户详细信息")
    @GetMapping("/profile/{mobile}")
    public AppUserResponse getProfileByMobile(
            @Parameter(description = "手机号", example = "13888888888")
            @PathVariable("mobile")
            @NotBlank(message = "手机号不能为空")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            String mobile) {
        Optional<AppUserResponse> result = appUserService.getByMobile(mobile);
        return result.orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Operation(summary = "根据用户ID获取用户信息", description = "通过用户ID查询用户详细信息")
    @GetMapping("/profile/id/{id}")
    public AppUserResponse getProfileById(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        Optional<AppUserResponse> result = appUserService.getById(id);
        return result.orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Operation(summary = "更新用户信息", description = "更新APP用户个人信息")
    @PutMapping("/profile")
    public AppUserResponse updateProfile(@Valid @RequestBody AppUserUpdateRequest request) {
        Optional<AppUserResponse> result = appUserService.updateProfile(request);
        return result.orElseThrow(() -> new RuntimeException("用户不存在或更新失败"));
    }

    @Operation(summary = "禁用用户", description = "将用户状态设置为禁用")
    @PutMapping("/{id}/disable")
    public Boolean disableUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        return appUserService.updateStatus(id, 0);
    }

    @Operation(summary = "启用用户", description = "将用户状态设置为正常")
    @PutMapping("/{id}/enable")
    public Boolean enableUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        return appUserService.updateStatus(id, 1);
    }

    @Operation(summary = "更新最后登录时间", description = "用户登录时更新最后登录时间")
    @PutMapping("/{id}/last-login")
    public Boolean updateLastLoginTime(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        return appUserService.updateLastLoginTime(id);
    }

    @Operation(summary = "检查手机号是否已注册", description = "检查手机号是否已被注册")
    @GetMapping("/check/mobile")
    public Boolean checkMobile(
            @Parameter(description = "手机号", example = "13888888888")
            @RequestParam("mobile")
            @NotBlank(message = "手机号不能为空")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            String mobile) {
        return !appUserService.existsByMobile(mobile);
    }

    @Operation(summary = "检查用户名是否已存在", description = "检查用户名是否已被使用")
    @GetMapping("/check/username")
    public Boolean checkUsername(
            @Parameter(description = "用户名", example = "testuser")
            @RequestParam("username") String username,
            @Parameter(description = "排除的用户ID，更新时使用")
            @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return !appUserService.existsByUsername(username, excludeId);
    }

    // ========================================
    // 软删除相关接口
    // ========================================

    @Operation(summary = "软删除用户", description = "将用户标记为已删除，可恢复")
    @DeleteMapping("/{id}")
    public Boolean softDeleteUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        return appUserService.softDeleteUser(id);
    }

    @Operation(summary = "恢复已删除用户", description = "恢复之前被软删除的用户")
    @PutMapping("/{id}/restore")
    public Boolean restoreUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        return appUserService.restoreUser(id);
    }

    @Operation(summary = "查询已删除用户", description = "获取已删除用户信息")
    @GetMapping("/deleted/{id}")
    public AppUserResponse getDeletedUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        Optional<AppUserResponse> result = appUserService.getDeletedById(id);
        return result.orElseThrow(() -> new RuntimeException("已删除用户不存在"));
    }

    @Operation(summary = "获取已删除用户列表", description = "分页查询已删除用户列表")
    @GetMapping("/deleted")
    public List<AppUserResponse> getDeletedUsers(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return appUserService.getDeletedUsers(pageNum, pageSize);
    }

    @Operation(summary = "统计已删除用户数量", description = "获取已删除用户总数")
    @GetMapping("/deleted/count")
    public Long getDeletedCount() {
        return appUserService.getDeletedCount();
    }

    @Operation(summary = "物理删除用户", description = "永久删除用户数据，不可恢复（危险操作）")
    @DeleteMapping("/{id}/physical")
    public Boolean physicalDeleteUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable("id") Long id) {
        return appUserService.physicalDeleteUser(id);
    }
}
