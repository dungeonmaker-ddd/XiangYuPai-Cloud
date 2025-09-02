package com.xypai.chat.controller.app;

import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.core.web.page.TableDataInfo;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.security.annotation.RequiresPermissions;
import com.xypai.chat.domain.dto.ConversationAddDTO;
import com.xypai.chat.domain.dto.ConversationQueryDTO;
import com.xypai.chat.domain.dto.ConversationUpdateDTO;
import com.xypai.chat.domain.vo.ConversationDetailVO;
import com.xypai.chat.domain.vo.ConversationListVO;
import com.xypai.chat.service.IChatConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天会话控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "聊天会话", description = "聊天会话管理API")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Validated
public class ChatConversationController extends BaseController {

    private final IChatConversationService conversationService;

    /**
     * 查询会话列表
     */
    @Operation(summary = "查询会话列表", description = "分页查询会话列表信息")
    @GetMapping("/list")
    @RequiresPermissions("chat:conversation:list")
    public TableDataInfo list(ConversationQueryDTO query) {
        startPage();
        List<ConversationListVO> list = conversationService.selectConversationList(query);
        return getDataTable(list);
    }

    /**
     * 获取会话详细信息
     */
    @Operation(summary = "获取会话详细信息", description = "根据会话ID获取详细信息")
    @GetMapping("/{conversationId}")
    @RequiresPermissions("chat:conversation:query")
    @Log(title = "会话管理", businessType = BusinessType.QUERY)
    public R<ConversationDetailVO> getInfo(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        return R.ok(conversationService.selectConversationById(conversationId));
    }

    /**
     * 创建会话
     */
    @Operation(summary = "创建会话", description = "创建新的聊天会话")
    @PostMapping
    @RequiresPermissions("chat:conversation:add")
    @Log(title = "会话管理", businessType = BusinessType.INSERT)
    public R<Long> add(@Validated @RequestBody ConversationAddDTO conversationAddDTO) {
        return R.ok("会话创建成功", conversationService.createConversation(conversationAddDTO));
    }

    /**
     * 修改会话
     */
    @Operation(summary = "修改会话", description = "更新会话信息")
    @PutMapping
    @RequiresPermissions("chat:conversation:edit")
    @Log(title = "会话管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@Validated @RequestBody ConversationUpdateDTO conversationUpdateDTO) {
        return toAjax(conversationService.updateConversation(conversationUpdateDTO));
    }

    /**
     * 删除会话
     */
    @Operation(summary = "删除会话", description = "删除指定会话")
    @DeleteMapping("/{conversationId}")
    @RequiresPermissions("chat:conversation:remove")
    @Log(title = "会话管理", businessType = BusinessType.DELETE)
    public R<Void> remove(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        return toAjax(conversationService.deleteConversation(conversationId));
    }

    /**
     * 创建私聊会话
     */
    @Operation(summary = "创建私聊会话", description = "与指定用户创建私聊会话")
    @PostMapping("/private/{targetUserId}")
    @RequiresPermissions("chat:conversation:add")
    @Log(title = "创建私聊", businessType = BusinessType.INSERT)
    public R<Long> createPrivateConversation(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return R.ok("私聊会话创建成功", conversationService.createPrivateConversation(targetUserId));
    }

    /**
     * 创建群聊会话
     */
    @Operation(summary = "创建群聊会话", description = "创建群聊会话")
    @PostMapping("/group")
    @RequiresPermissions("chat:conversation:add")
    @Log(title = "创建群聊", businessType = BusinessType.INSERT)
    public R<Long> createGroupConversation(
            @Parameter(description = "群聊名称", required = true)
            @RequestParam String title,
            @Parameter(description = "群聊描述")
            @RequestParam(required = false) String description,
            @Parameter(description = "初始成员ID列表")
            @RequestBody(required = false) List<Long> memberIds) {
        return R.ok("群聊会话创建成功", 
            conversationService.createGroupConversation(title, description, memberIds));
    }

    /**
     * 创建订单会话
     */
    @Operation(summary = "创建订单会话", description = "为订单创建专用会话")
    @PostMapping("/order/{orderId}")
    @RequiresPermissions("chat:conversation:add")
    @Log(title = "创建订单会话", businessType = BusinessType.INSERT)
    public R<Long> createOrderConversation(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        return R.ok("订单会话创建成功", conversationService.createOrderConversation(orderId));
    }

    /**
     * 获取我的会话列表
     */
    @Operation(summary = "获取我的会话列表", description = "获取当前用户的会话列表")
    @GetMapping("/my")
    @RequiresPermissions("chat:conversation:query")
    public TableDataInfo getMyConversations(
            @Parameter(description = "会话类型")
            @RequestParam(required = false) Integer type) {
        startPage();
        List<ConversationListVO> list = conversationService.getMyConversations(type);
        return getDataTable(list);
    }

    /**
     * 邀请用户加入会话
     */
    @Operation(summary = "邀请用户加入会话", description = "邀请用户加入群聊会话")
    @PostMapping("/{conversationId}/invite")
    @RequiresPermissions("chat:conversation:edit")
    @Log(title = "邀请用户", businessType = BusinessType.UPDATE)
    public R<Void> inviteUsers(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        return toAjax(conversationService.inviteUsers(conversationId, userIds));
    }

    /**
     * 移除会话成员
     */
    @Operation(summary = "移除会话成员", description = "从会话中移除指定成员")
    @DeleteMapping("/{conversationId}/members/{userId}")
    @RequiresPermissions("chat:conversation:edit")
    @Log(title = "移除成员", businessType = BusinessType.DELETE)
    public R<Void> removeMember(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        return toAjax(conversationService.removeMember(conversationId, userId));
    }

    /**
     * 退出会话
     */
    @Operation(summary = "退出会话", description = "当前用户退出会话")
    @PostMapping("/{conversationId}/leave")
    @RequiresPermissions("chat:conversation:edit")
    @Log(title = "退出会话", businessType = BusinessType.UPDATE)
    public R<Void> leaveConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        return toAjax(conversationService.leaveConversation(conversationId));
    }

    /**
     * 设置会话管理员
     */
    @Operation(summary = "设置会话管理员", description = "设置群聊管理员")
    @PutMapping("/{conversationId}/admin/{userId}")
    @RequiresPermissions("chat:conversation:edit")
    @Log(title = "设置管理员", businessType = BusinessType.UPDATE)
    public R<Void> setAdmin(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "是否设为管理员", required = true)
            @RequestParam Boolean isAdmin) {
        return toAjax(conversationService.setAdmin(conversationId, userId, isAdmin));
    }

    /**
     * 禁言用户
     */
    @Operation(summary = "禁言用户", description = "禁言会话中的用户")
    @PutMapping("/{conversationId}/mute/{userId}")
    @RequiresPermissions("chat:conversation:edit")
    @Log(title = "禁言用户", businessType = BusinessType.UPDATE)
    public R<Void> muteUser(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "是否禁言", required = true)
            @RequestParam Boolean muted) {
        return toAjax(conversationService.muteUser(conversationId, userId, muted));
    }

    /**
     * 归档会话
     */
    @Operation(summary = "归档会话", description = "归档指定会话")
    @PutMapping("/{conversationId}/archive")
    @RequiresPermissions("chat:conversation:edit")
    @Log(title = "归档会话", businessType = BusinessType.UPDATE)
    public R<Void> archiveConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        return toAjax(conversationService.archiveConversation(conversationId));
    }

    /**
     * 恢复会话
     */
    @Operation(summary = "恢复会话", description = "恢复已归档的会话")
    @PutMapping("/{conversationId}/restore")
    @RequiresPermissions("chat:conversation:edit")
    @Log(title = "恢复会话", businessType = BusinessType.UPDATE)
    public R<Void> restoreConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        return toAjax(conversationService.restoreConversation(conversationId));
    }

    /**
     * 获取会话成员列表
     */
    @Operation(summary = "获取会话成员列表", description = "获取指定会话的成员列表")
    @GetMapping("/{conversationId}/members")
    @RequiresPermissions("chat:conversation:query")
    public TableDataInfo getConversationMembers(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        startPage();
        List<Map<String, Object>> list = conversationService.getConversationMembers(conversationId);
        return getDataTable(list);
    }

    /**
     * 获取会话统计信息
     */
    @Operation(summary = "获取会话统计信息", description = "获取会话的统计数据")
    @GetMapping("/{conversationId}/statistics")
    @RequiresPermissions("chat:conversation:query")
    public R<Map<String, Object>> getConversationStatistics(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        return R.ok(conversationService.getConversationStatistics(conversationId));
    }
}
