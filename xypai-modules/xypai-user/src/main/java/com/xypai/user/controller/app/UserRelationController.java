package com.xypai.user.controller.app;

import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.core.web.page.TableDataInfo;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.security.annotation.RequiresPermissions;
import com.xypai.user.domain.dto.UserRelationQueryDTO;
import com.xypai.user.domain.vo.UserRelationVO;
import com.xypai.user.service.IUserRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户关系控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "用户关系", description = "用户关系管理API")
@RestController
@RequestMapping("/api/v1/relations")
@RequiredArgsConstructor
@Validated
public class UserRelationController extends BaseController {

    private final IUserRelationService userRelationService;

    /**
     * 关注用户
     */
    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/follow/{targetUserId}")
    @RequiresPermissions("user:relation:add")
    @Log(title = "关注用户", businessType = BusinessType.INSERT)
    public R<Void> followUser(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return R.result(userRelationService.followUser(targetUserId));
    }

    /**
     * 取消关注
     */
    @Operation(summary = "取消关注", description = "取消关注指定用户")
    @DeleteMapping("/follow/{targetUserId}")
    @RequiresPermissions("user:relation:remove")
    @Log(title = "取消关注", businessType = BusinessType.DELETE)
    public R<Void> unfollowUser(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return R.result(userRelationService.unfollowUser(targetUserId));
    }

    /**
     * 拉黑用户
     */
    @Operation(summary = "拉黑用户", description = "拉黑指定用户")
    @PostMapping("/block/{targetUserId}")
    @RequiresPermissions("user:relation:add")
    @Log(title = "拉黑用户", businessType = BusinessType.INSERT)
    public R<Void> blockUser(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return R.result(userRelationService.blockUser(targetUserId));
    }

    /**
     * 取消拉黑
     */
    @Operation(summary = "取消拉黑", description = "取消拉黑指定用户")
    @DeleteMapping("/block/{targetUserId}")
    @RequiresPermissions("user:relation:remove")
    @Log(title = "取消拉黑", businessType = BusinessType.DELETE)
    public R<Void> unblockUser(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return R.result(userRelationService.unblockUser(targetUserId));
    }

    /**
     * 获取关注列表
     */
    @Operation(summary = "获取关注列表", description = "获取当前用户的关注列表")
    @GetMapping("/following")
    @RequiresPermissions("user:relation:query")
    public TableDataInfo getFollowingList(UserRelationQueryDTO query) {
        startPage();
        List<UserRelationVO> list = userRelationService.getFollowingList(query);
        return getDataTable(list);
    }

    /**
     * 获取粉丝列表
     */
    @Operation(summary = "获取粉丝列表", description = "获取当前用户的粉丝列表")
    @GetMapping("/followers")
    @RequiresPermissions("user:relation:query")
    public TableDataInfo getFollowersList(UserRelationQueryDTO query) {
        startPage();
        List<UserRelationVO> list = userRelationService.getFollowersList(query);
        return getDataTable(list);
    }

    /**
     * 获取拉黑列表
     */
    @Operation(summary = "获取拉黑列表", description = "获取当前用户的拉黑列表")
    @GetMapping("/blocked")
    @RequiresPermissions("user:relation:query")
    public TableDataInfo getBlockedList(UserRelationQueryDTO query) {
        startPage();
        List<UserRelationVO> list = userRelationService.getBlockedList(query);
        return getDataTable(list);
    }

    /**
     * 获取指定用户关注列表
     */
    @Operation(summary = "获取指定用户关注列表", description = "获取指定用户的关注列表")
    @GetMapping("/{userId}/following")
    @RequiresPermissions("user:relation:query")
    public TableDataInfo getUserFollowingList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            UserRelationQueryDTO query) {
        startPage();
        List<UserRelationVO> list = userRelationService.getUserFollowingList(userId, query);
        return getDataTable(list);
    }

    /**
     * 获取指定用户粉丝列表
     */
    @Operation(summary = "获取指定用户粉丝列表", description = "获取指定用户的粉丝列表")
    @GetMapping("/{userId}/followers")
    @RequiresPermissions("user:relation:query")
    public TableDataInfo getUserFollowersList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            UserRelationQueryDTO query) {
        startPage();
        List<UserRelationVO> list = userRelationService.getUserFollowersList(userId, query);
        return getDataTable(list);
    }

    /**
     * 检查用户关系
     */
    @Operation(summary = "检查用户关系", description = "检查与指定用户的关系状态")
    @GetMapping("/check/{targetUserId}")
    @RequiresPermissions("user:relation:query")
    public R<Map<String, Boolean>> checkUserRelation(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return R.ok(userRelationService.checkUserRelation(targetUserId));
    }

    /**
     * 获取关系统计
     */
    @Operation(summary = "获取关系统计", description = "获取用户关系统计数据")
    @GetMapping("/statistics")
    @RequiresPermissions("user:relation:query")
    public R<Map<String, Long>> getRelationStatistics() {
        return R.ok(userRelationService.getRelationStatistics());
    }

    /**
     * 获取指定用户关系统计
     */
    @Operation(summary = "获取指定用户关系统计", description = "获取指定用户的关系统计数据")
    @GetMapping("/{userId}/statistics")
    @RequiresPermissions("user:relation:query")
    public R<Map<String, Long>> getUserRelationStatistics(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        return R.ok(userRelationService.getUserRelationStatistics(userId));
    }

    /**
     * 批量关注
     */
    @Operation(summary = "批量关注", description = "批量关注多个用户")
    @PostMapping("/batch-follow")
    @RequiresPermissions("user:relation:add")
    @Log(title = "批量关注", businessType = BusinessType.INSERT)
    public R<Void> batchFollowUsers(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        return R.result(userRelationService.batchFollowUsers(userIds));
    }

    /**
     * 批量取消关注
     */
    @Operation(summary = "批量取消关注", description = "批量取消关注多个用户")
    @PostMapping("/batch-unfollow")
    @RequiresPermissions("user:relation:remove")
    @Log(title = "批量取消关注", businessType = BusinessType.DELETE)
    public R<Void> batchUnfollowUsers(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        return R.result(userRelationService.batchUnfollowUsers(userIds));
    }
}
