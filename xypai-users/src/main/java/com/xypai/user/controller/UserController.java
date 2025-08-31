package com.xypai.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.core.web.page.TableDataInfo;
import com.xypai.common.datascope.annotation.DataScope;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.security.annotation.RequiresPermissions;
import com.xypai.user.constant.UserConstants;
import com.xypai.user.domain.dto.UserAddDTO;
import com.xypai.user.domain.dto.UserQueryDTO;
import com.xypai.user.domain.dto.UserUpdateDTO;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.vo.UserDetailVO;
import com.xypai.user.domain.vo.UserListVO;
import com.xypai.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 🏗️ XY相遇派用户控制器 - 企业架构实现
 * <p>
 * 遵循企业微服务架构规范：
 * - 继承BaseController获得通用功能
 * - 使用@RequiresPermissions进行权限控制
 * - 使用@Log注解记录操作日志
 * - 使用@DataScope进行数据权限控制
 * - 统一返回R<T>响应格式
 * - 完整的API文档注解
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "🏗️ XY相遇派用户管理",
        description = "基于企业架构的用户管理API - 提供用户注册、查询、更新、状态管理等核心功能")
public class UserController extends BaseController {

    private final IUserService userService;

    // ================================
    // 🚀 系统监控
    // ================================

    /**
     * 健康检查
     */
    @Operation(summary = "🚀 服务健康检查", description = "检查XY相遇派用户服务运行状态")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "服务正常运行",
                    content = @Content(schema = @Schema(implementation = R.class)))
    })
    @GetMapping("/health")
    public R<String> health() {
        return R.ok("XY相遇派用户微服务运行正常 🚀 - 企业架构实现");
    }

    // ================================
    // 📝 用户注册与创建
    // ================================

    /**
     * 注册新用户
     */
    @Operation(summary = "📝 用户注册",
            description = "注册新用户到XY相遇派平台，支持手机号和用户名注册")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "参数验证失败：手机号格式错误、用户名不符合规范等"),
            @ApiResponse(responseCode = "409", description = "手机号或用户名已存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/register")
    @RequiresPermissions(UserConstants.USER_ADD_PERMISSION)
    @Log(title = "用户注册", businessType = BusinessType.INSERT)
    public R<User> registerUser(@Valid @RequestBody UserAddDTO addDTO) {
        log.info("收到用户注册请求: mobile={}, username={}", addDTO.mobile(), addDTO.username());

        User user = userService.registerUser(addDTO);
        return R.ok(user);
    }

    // ================================
    // 🔍 用户查询
    // ================================

    /**
     * 根据ID获取用户详情
     */
    @Operation(summary = "🔍 根据ID查询用户", description = "通过用户ID获取用户详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = UserDetailVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{id}")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    @Log(title = "查询用户详情", businessType = BusinessType.OTHER)
    public R<UserDetailVO> getUserById(
            @Parameter(description = "用户ID", required = true, example = "100000")
            @PathVariable Long id) {

        UserDetailVO userDetail = userService.getUserDetail(id);
        return R.ok(userDetail);
    }

    /**
     * 根据手机号查询用户
     */
    @Operation(summary = "📱 根据手机号查询用户", description = "通过手机号获取用户信息")
    @GetMapping("/mobile/{mobile}")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<User> getUserByMobile(@PathVariable String mobile) {
        return userService.findByMobile(mobile)
                .map(user -> R.ok(user))
                .orElse(R.fail("用户不存在"));
    }

    /**
     * 根据用户名查询用户
     */
    @Operation(summary = "👤 根据用户名查询用户", description = "通过用户名获取用户信息")
    @GetMapping("/username/{username}")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<User> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> R.ok(user))
                .orElse(R.fail("用户不存在"));
    }

    /**
     * 根据用户编码查询用户
     */
    @Operation(summary = "🔢 根据用户编码查询用户", description = "通过用户编码获取用户信息")
    @GetMapping("/code/{userCode}")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<User> getUserByCode(@PathVariable String userCode) {
        return userService.findByUserCode(userCode)
                .map(user -> R.ok(user))
                .orElse(R.fail("用户不存在"));
    }

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "📋 分页查询用户列表",
            description = "支持多条件查询和数据权限的用户分页查询，包括按状态、类型、时间范围等筛选")
    @GetMapping("/page")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    @DataScope(deptAlias = "d", userAlias = "u")
    public TableDataInfo getUsersPage(UserQueryDTO query) {
        startPage();
        Page<UserListVO> page = new Page<>();
        IPage<UserListVO> result = userService.selectUserList(page, query);
        return getDataTable(result.getRecords());
    }

    /**
     * 查询VIP用户列表
     */
    @Operation(summary = "💎 查询VIP用户列表", description = "查询VIP及以上等级的用户")
    @GetMapping("/vip")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public TableDataInfo getVipUsers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<UserListVO> page = new Page<>(current, size);
        IPage<UserListVO> result = userService.findVipUsers(page);
        return getDataTable(result.getRecords());
    }

    /**
     * 查询活跃用户列表
     */
    @Operation(summary = "🔥 查询活跃用户列表", description = "查询最近活跃的用户")
    @GetMapping("/active")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public TableDataInfo getActiveUsers(
            @Parameter(description = "最近几天", example = "30")
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<UserListVO> page = new Page<>(current, size);
        IPage<UserListVO> result = userService.findActiveUsers(days, page);
        return getDataTable(result.getRecords());
    }

    /**
     * 查询新用户列表
     */
    @Operation(summary = "🆕 查询新用户列表", description = "查询最近注册的用户")
    @GetMapping("/new")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public TableDataInfo getNewUsers(
            @Parameter(description = "最近几天", example = "7")
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<UserListVO> page = new Page<>(current, size);
        IPage<UserListVO> result = userService.findNewUsers(days, page);
        return getDataTable(result.getRecords());
    }

    // ================================
    // ✏️ 用户信息更新
    // ================================

    /**
     * 更新用户信息
     */
    @Operation(summary = "✏️ 更新用户信息", description = "更新用户基础信息")
    @PutMapping
    @RequiresPermissions(UserConstants.USER_EDIT_PERMISSION)
    @Log(title = "更新用户信息", businessType = BusinessType.UPDATE)
    public R<Void> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
        boolean success = userService.updateUser(updateDTO);
        return success ? R.ok() : R.fail();
    }

    // ================================
    // 🔄 用户状态管理
    // ================================

    /**
     * 批量更新用户状态
     */
    @Operation(summary = "🔄 批量更新用户状态", description = "批量更新用户状态")
    @PutMapping("/status")
    @RequiresPermissions(UserConstants.USER_STATUS_PERMISSION)
    @Log(title = "批量更新用户状态", businessType = BusinessType.UPDATE)
    public R<String> updateUserStatus(@RequestBody UserStatusUpdateRequest request) {
        log.info("批量更新用户状态: userIds={}, newStatus={}, operatorId={}",
                request.userIds(), request.newStatus(), request.operatorId());

        int updated = userService.batchUpdateStatus(request.userIds(), request.newStatus(), request.operatorId());
        return R.ok("成功更新 " + updated + " 个用户状态");
    }

    /**
     * 用户升级VIP
     */
    @Operation(summary = "💎 用户升级VIP", description = "升级用户类型")
    @PutMapping("/{id}/upgrade")
    @RequiresPermissions(UserConstants.USER_UPGRADE_PERMISSION)
    @Log(title = "用户类型升级", businessType = BusinessType.UPDATE)
    public R<Void> upgradeUser(
            @PathVariable Long id,
            @RequestParam Integer userType,
            @RequestParam String operatorId) {

        boolean success = userService.upgradeUserType(id, userType, operatorId);
        return success ? R.ok() : R.fail();
    }

    /**
     * 用户实名认证
     */
    @Operation(summary = "✅ 用户实名认证", description = "完成用户实名认证")
    @PutMapping("/{id}/verify")
    @RequiresPermissions(UserConstants.USER_VERIFY_PERMISSION)
    @Log(title = "用户实名认证", businessType = BusinessType.UPDATE)
    public R<Void> verifyUser(
            @PathVariable Long id,
            @RequestParam String realName,
            @RequestParam String operatorId) {

        boolean success = userService.verifyRealName(id, realName, operatorId);
        return success ? R.ok() : R.fail();
    }

    // ================================
    // 🗑️ 用户删除
    // ================================

    /**
     * 删除用户
     */
    @Operation(summary = "🗑️ 删除用户", description = "删除指定用户(支持批量)")
    @DeleteMapping("/{ids}")
    @RequiresPermissions(UserConstants.USER_REMOVE_PERMISSION)
    @Log(title = "删除用户", businessType = BusinessType.DELETE)
    public R<Void> removeUsers(@PathVariable Long[] ids) {
        boolean success = userService.removeByIds(Arrays.asList(ids));
        return success ? R.ok() : R.fail();
    }

    // ================================
    // 📊 统计查询
    // ================================

    /**
     * 获取用户总数统计
     */
    @Operation(summary = "📊 用户总数统计", description = "获取各类用户数量统计")
    @GetMapping("/stats/total")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<UserTotalStats> getTotalStats() {
        UserTotalStats stats = new UserTotalStats(
                userService.countAllUsers(),
                userService.countActiveUsers(),
                userService.countVerifiedUsers(),
                userService.countVipUsers()
        );
        return R.ok(stats);
    }

    /**
     * 获取用户类型分布统计
     */
    @Operation(summary = "📈 用户类型分布", description = "获取用户类型分布统计")
    @GetMapping("/stats/type")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<List<Map<String, Object>>> getUserTypeStats() {
        List<Map<String, Object>> stats = userService.countByUserType();
        return R.ok(stats);
    }

    /**
     * 获取平台分布统计
     */
    @Operation(summary = "📱 平台分布统计", description = "获取各平台用户分布")
    @GetMapping("/stats/platform")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<List<Map<String, Object>>> getPlatformStats() {
        List<Map<String, Object>> stats = userService.countByPlatform();
        return R.ok(stats);
    }

    /**
     * 获取注册渠道统计
     */
    @Operation(summary = "🎯 注册渠道统计", description = "获取各渠道注册用户分布")
    @GetMapping("/stats/channel")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<List<Map<String, Object>>> getChannelStats() {
        List<Map<String, Object>> stats = userService.countBySourceChannel();
        return R.ok(stats);
    }

    /**
     * 获取地区分布统计
     */
    @Operation(summary = "🌍 地区分布统计", description = "获取用户地区分布TOP统计")
    @GetMapping("/stats/location")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<List<Map<String, Object>>> getLocationStats(
            @Parameter(description = "TOP数量", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> stats = userService.getTopLocationStats(limit);
        return R.ok(stats);
    }

    /**
     * 获取用户活跃度统计
     */
    @Operation(summary = "🔥 用户活跃度统计", description = "获取用户活跃度相关统计")
    @GetMapping("/stats/activity")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<Map<String, Object>> getActivityStats() {
        Map<String, Object> stats = userService.getUserActivityStats();
        return R.ok(stats);
    }

    /**
     * 获取用户注册趋势
     */
    @Operation(summary = "📈 用户注册趋势", description = "获取指定时间段的用户注册趋势")
    @GetMapping("/stats/trend")
    @RequiresPermissions(UserConstants.USER_QUERY_PERMISSION)
    public R<List<Map<String, Object>>> getRegistrationTrend(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<Map<String, Object>> trend = userService.getUserRegistrationTrend(startTime, endTime);
        return R.ok(trend);
    }

    // ================================
    // ✅ 验证接口
    // ================================

    /**
     * 检查手机号是否存在
     */
    @Operation(summary = "📱 检查手机号", description = "验证手机号是否已被注册")
    @GetMapping("/check/mobile")
    public R<Boolean> checkMobile(@RequestParam String mobile) {
        boolean exists = userService.existsByMobile(mobile);
        return R.ok(exists);
    }

    /**
     * 检查用户名是否存在
     */
    @Operation(summary = "👤 检查用户名", description = "验证用户名是否已被使用")
    @GetMapping("/check/username")
    public R<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return R.ok(exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @Operation(summary = "📧 检查邮箱", description = "验证邮箱是否已被使用")
    @GetMapping("/check/email")
    public R<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return R.ok(exists);
    }

    /**
     * 检查用户编码是否存在
     */
    @Operation(summary = "🔢 检查用户编码", description = "验证用户编码是否已被使用")
    @GetMapping("/check/code")
    public R<Boolean> checkUserCode(@RequestParam String userCode) {
        boolean exists = userService.existsByUserCode(userCode);
        return R.ok(exists);
    }

    // ================================
    // 📝 DTO Records
    // ================================

    /**
     * 用户状态更新请求
     */
    @Schema(description = "用户状态更新请求")
    public record UserStatusUpdateRequest(
            @Schema(description = "用户ID列表", required = true)
            List<Long> userIds,

            @Schema(description = "新状态", example = "1", required = true)
            Integer newStatus,

            @Schema(description = "操作员ID", required = true)
            String operatorId
    ) {
    }

    /**
     * 用户总数统计
     */
    @Schema(description = "用户总数统计")
    public record UserTotalStats(
            @Schema(description = "总用户数")
            Long totalUsers,

            @Schema(description = "活跃用户数")
            Long activeUsers,

            @Schema(description = "实名认证用户数")
            Long verifiedUsers,

            @Schema(description = "VIP用户数")
            Long vipUsers
    ) {
    }
}
