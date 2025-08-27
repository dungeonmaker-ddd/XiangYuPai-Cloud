package com.xypai.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xypai.user.domain.record.UserCreateRequest;
import com.xypai.user.domain.record.UserQueryRequest;
import com.xypai.user.domain.record.UserResponse;
import com.xypai.user.domain.record.UserUpdateRequest;
import com.xypai.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户管理接口
 *
 * @author XyPai
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理接口", description = "用户CRUD操作相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "创建用户", description = "新增用户，支持员工或教师")
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "更新用户信息", description = "根据用户ID更新用户信息")
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        Optional<UserResponse> result = userService.updateUser(request.withId(id));
        return result.orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户详细信息")
    @GetMapping("/{id}")
    public UserResponse getUserById(
            @Parameter(description = "用户ID", example = "1") @PathVariable("id") Long id) {
        Optional<UserResponse> result = userService.getUserById(id);
        return result.orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Operation(summary = "根据用户名查询用户", description = "根据用户名查询用户信息")
    @GetMapping("/username/{username}")
    public UserResponse getUserByUsername(
            @Parameter(description = "用户名", example = "admin") @PathVariable("username") String username) {
        Optional<UserResponse> result = userService.getUserByUsername(username);
        return result.orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Operation(summary = "根据邮箱查询用户", description = "根据邮箱查询用户信息")
    @GetMapping("/email/{email}")
    public UserResponse getUserByEmail(
            @Parameter(description = "邮箱", example = "admin@xypai.com") @PathVariable("email") String email) {
        Optional<UserResponse> result = userService.getUserByEmail(email);
        return result.orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Operation(summary = "根据手机号查询用户", description = "根据手机号查询用户信息")
    @GetMapping("/phone/{phone}")
    public UserResponse getUserByPhone(
            @Parameter(description = "手机号", example = "13888888888") @PathVariable("phone") String phone) {
        Optional<UserResponse> result = userService.getUserByPhone(phone);
        return result.orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Operation(summary = "分页查询用户列表", description = "支持条件查询的分页用户列表")
    @PostMapping("/page")
    public IPage<UserResponse> getUserPage(@Valid @RequestBody UserQueryRequest request) {
        return userService.getUserPage(request);
    }

    @Operation(summary = "根据状态查询用户列表", description = "根据用户状态查询用户列表")
    @GetMapping("/status/{status}")
    public List<UserResponse> getUsersByStatus(
            @Parameter(description = "用户状态", example = "0") @PathVariable("status") Integer status) {
        return userService.getUsersByStatus(status);
    }

    @Operation(summary = "删除用户", description = "逻辑删除用户")
    @DeleteMapping("/{id}")
    public Boolean deleteUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable("id") Long id) {
        return userService.deleteUser(id);
    }

    @Operation(summary = "批量删除用户", description = "批量逻辑删除用户")
    @DeleteMapping("/batch")
    public Boolean deleteUsers(@Valid @RequestBody @NotEmpty List<@NotNull Long> userIds) {
        return userService.deleteUsers(userIds);
    }

    @Operation(summary = "激活用户", description = "将用户状态设置为正常")
    @PutMapping("/{id}/activate")
    public Boolean activateUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable("id") Long id) {
        return userService.activateUser(id);
    }

    @Operation(summary = "停用用户", description = "将用户状态设置为停用")
    @PutMapping("/{id}/deactivate")
    public Boolean deactivateUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable("id") Long id) {
        return userService.deactivateUser(id);
    }

    @Operation(summary = "批量更新用户状态", description = "批量更新多个用户的状态")
    @PutMapping("/status/batch")
    public Boolean updateUserStatus(
            @Valid @RequestBody BatchUpdateStatusRequest request) {
        return userService.updateUserStatus(request.userIds(), request.status());
    }

    @Operation(summary = "重置用户密码", description = "重置用户密码为默认密码")
    @PutMapping("/{id}/password/reset")
    public Boolean resetUserPassword(
            @Parameter(description = "用户ID", example = "1") @PathVariable("id") Long id) {
        return userService.resetUserPassword(id, "123456"); // 默认密码
    }

    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已被使用")
    @GetMapping("/check/username")
    public Boolean checkUsername(
            @Parameter(description = "用户名", example = "admin") @RequestParam("username") String username,
            @Parameter(description = "排除的用户ID，更新时使用") @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return !userService.existsUsername(username, excludeId);
    }

    @Operation(summary = "检查邮箱是否存在", description = "检查邮箱是否已被使用")
    @GetMapping("/check/email")
    public Boolean checkEmail(
            @Parameter(description = "邮箱", example = "admin@xypai.com") @RequestParam("email") String email,
            @Parameter(description = "排除的用户ID，更新时使用") @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return !userService.existsEmail(email, excludeId);
    }

    @Operation(summary = "检查手机号是否存在", description = "检查手机号是否已被使用")
    @GetMapping("/check/phone")
    public Boolean checkPhone(
            @Parameter(description = "手机号", example = "13888888888") @RequestParam("phone") String phone,
            @Parameter(description = "排除的用户ID，更新时使用") @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return !userService.existsPhone(phone, excludeId);
    }

    @Operation(summary = "获取用户统计信息", description = "获取用户总数和活跃用户数")
    @GetMapping("/stats")
    public UserStatsResponse getUserStats() {
        Long totalCount = userService.getUserCount();
        Long activeCount = userService.getActiveUserCount();
        return new UserStatsResponse(totalCount, activeCount, totalCount - activeCount);
    }

    /**
     * 批量状态更新请求记录
     */
    public record BatchUpdateStatusRequest(
            @NotEmpty(message = "用户ID列表不能为空")
            List<@NotNull Long> userIds,

            @NotNull(message = "状态不能为空")
            Integer status
    ) {
        public BatchUpdateStatusRequest {
            if (status < 0 || status > 1) {
                throw new IllegalArgumentException("状态值必须为0或1");
            }
        }
    }

    /**
     * 用户统计响应记录
     */
    public record UserStatsResponse(
            Long totalCount,
            Long activeCount,
            Long inactiveCount
    ) {
    }
}
