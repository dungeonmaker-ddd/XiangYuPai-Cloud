package com.xypai.user.insurance.web;

import com.xypai.user.application.command.CreateUserCommand;
import com.xypai.user.application.command.UpdateUserCommand;
import com.xypai.user.application.service.UserApplicationService;
import com.xypai.user.domain.repository.UserRepository;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.insurance.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户DDD控制器 - 接口层
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "基于DDD架构的用户管理接口")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;
    private final UserRepository userRepository;

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserCommand command) {
        log.info("创建用户请求: {}", command);

        var userId = userApplicationService.createUser(command);
        var userAggregate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户创建失败"));

        var response = UserResponse.from(userAggregate);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新用户基本信息")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserCommand command) {

        log.info("更新用户请求: userId={}, command={}", userId, command);

        // 确保路径参数和请求体中的用户ID一致
        var updateCommand = new UpdateUserCommand(
                UserId.of(userId),
                command.username(),
                command.nickname(),
                command.avatar(),
                command.gender(),
                command.birthDate()
        );

        userApplicationService.updateUser(updateCommand);

        var userAggregate = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        var response = UserResponse.from(userAggregate);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID查询用户
     */
    @Operation(summary = "根据ID查询用户", description = "通过用户ID获取用户信息")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        log.info("查询用户: userId={}", userId);

        var userAggregate = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        var response = UserResponse.from(userAggregate);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据手机号查询用户
     */
    @Operation(summary = "根据手机号查询用户", description = "通过手机号获取用户信息")
    @GetMapping("/mobile/{mobile}")
    public ResponseEntity<UserResponse> getUserByMobile(
            @Parameter(description = "手机号") @PathVariable String mobile) {

        log.info("根据手机号查询用户: mobile={}", mobile);

        var userAggregate = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        var response = UserResponse.from(userAggregate);
        return ResponseEntity.ok(response);
    }

    /**
     * 启用用户
     */
    @Operation(summary = "启用用户", description = "启用被禁用的用户")
    @PutMapping("/{userId}/enable")
    public ResponseEntity<Void> enableUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        log.info("启用用户: userId={}", userId);
        userApplicationService.enableUser(UserId.of(userId));
        return ResponseEntity.ok().build();
    }

    /**
     * 禁用用户
     */
    @Operation(summary = "禁用用户", description = "禁用用户账号")
    @PutMapping("/{userId}/disable")
    public ResponseEntity<Void> disableUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        log.info("禁用用户: userId={}", userId);
        userApplicationService.disableUser(UserId.of(userId));
        return ResponseEntity.ok().build();
    }

    /**
     * 更新最后登录时间
     */
    @Operation(summary = "更新最后登录时间", description = "用户登录时调用")
    @PutMapping("/{userId}/login")
    public ResponseEntity<Void> updateLastLogin(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        log.info("更新最后登录时间: userId={}", userId);
        userApplicationService.updateLastLogin(UserId.of(userId));
        return ResponseEntity.ok().build();
    }

    /**
     * 检查手机号可用性
     */
    @Operation(summary = "检查手机号可用性", description = "检查手机号是否可以注册")
    @GetMapping("/check/mobile")
    public ResponseEntity<Boolean> checkMobileAvailable(
            @Parameter(description = "手机号") @RequestParam String mobile) {

        boolean available = !userRepository.existsByMobile(mobile);
        return ResponseEntity.ok(available);
    }

    /**
     * 检查用户名可用性
     */
    @Operation(summary = "检查用户名可用性", description = "检查用户名是否可以使用")
    @GetMapping("/check/username")
    public ResponseEntity<Boolean> checkUsernameAvailable(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "排除的用户ID") @RequestParam(required = false) Long excludeUserId) {

        var excludeId = excludeUserId != null ? UserId.of(excludeUserId) : null;
        boolean available = !userRepository.existsByUsername(username, excludeId);
        return ResponseEntity.ok(available);
    }
}