package com.xypai.content.controller.app;

import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.core.web.page.TableDataInfo;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.security.annotation.RequiresPermissions;
import com.xypai.content.domain.dto.ContentActionAddDTO;
import com.xypai.content.domain.dto.ContentActionQueryDTO;
import com.xypai.content.domain.vo.ContentActionVO;
import com.xypai.content.service.IContentActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内容行为控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "内容行为", description = "内容互动行为管理API")
@RestController
@RequestMapping("/api/v1/content-actions")
@RequiredArgsConstructor
@Validated
public class ContentActionController extends BaseController {

    private final IContentActionService contentActionService;

    /**
     * 点赞内容
     */
    @Operation(summary = "点赞内容", description = "对指定内容进行点赞")
    @PostMapping("/like/{contentId}")
    @RequiresPermissions("content:action:add")
    @Log(title = "点赞内容", businessType = BusinessType.INSERT)
    public R<Void> likeContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return toAjax(contentActionService.likeContent(contentId));
    }

    /**
     * 取消点赞
     */
    @Operation(summary = "取消点赞", description = "取消对指定内容的点赞")
    @DeleteMapping("/like/{contentId}")
    @RequiresPermissions("content:action:remove")
    @Log(title = "取消点赞", businessType = BusinessType.DELETE)
    public R<Void> unlikeContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return toAjax(contentActionService.unlikeContent(contentId));
    }

    /**
     * 评论内容
     */
    @Operation(summary = "评论内容", description = "对指定内容进行评论")
    @PostMapping("/comment")
    @RequiresPermissions("content:action:add")
    @Log(title = "评论内容", businessType = BusinessType.INSERT)
    public R<Void> commentContent(@Validated @RequestBody ContentActionAddDTO commentDTO) {
        return toAjax(contentActionService.commentContent(commentDTO));
    }

    /**
     * 分享内容
     */
    @Operation(summary = "分享内容", description = "分享指定内容")
    @PostMapping("/share/{contentId}")
    @RequiresPermissions("content:action:add")
    @Log(title = "分享内容", businessType = BusinessType.INSERT)
    public R<Void> shareContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return toAjax(contentActionService.shareContent(contentId));
    }

    /**
     * 收藏内容
     */
    @Operation(summary = "收藏内容", description = "收藏指定内容")
    @PostMapping("/collect/{contentId}")
    @RequiresPermissions("content:action:add")
    @Log(title = "收藏内容", businessType = BusinessType.INSERT)
    public R<Void> collectContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return toAjax(contentActionService.collectContent(contentId));
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏", description = "取消收藏指定内容")
    @DeleteMapping("/collect/{contentId}")
    @RequiresPermissions("content:action:remove")
    @Log(title = "取消收藏", businessType = BusinessType.DELETE)
    public R<Void> uncollectContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return toAjax(contentActionService.uncollectContent(contentId));
    }

    /**
     * 活动报名
     */
    @Operation(summary = "活动报名", description = "报名参加活动")
    @PostMapping("/signup")
    @RequiresPermissions("content:action:add")
    @Log(title = "活动报名", businessType = BusinessType.INSERT)
    public R<Void> signupActivity(@Validated @RequestBody ContentActionAddDTO signupDTO) {
        return toAjax(contentActionService.signupActivity(signupDTO));
    }

    /**
     * 取消报名
     */
    @Operation(summary = "取消报名", description = "取消活动报名")
    @DeleteMapping("/signup/{contentId}")
    @RequiresPermissions("content:action:remove")
    @Log(title = "取消报名", businessType = BusinessType.DELETE)
    public R<Void> cancelSignup(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return toAjax(contentActionService.cancelSignup(contentId));
    }

    /**
     * 获取内容行为列表
     */
    @Operation(summary = "获取内容行为列表", description = "分页查询内容行为记录")
    @GetMapping("/list")
    @RequiresPermissions("content:action:list")
    public TableDataInfo list(ContentActionQueryDTO query) {
        startPage();
        List<ContentActionVO> list = contentActionService.selectActionList(query);
        return getDataTable(list);
    }

    /**
     * 获取内容的点赞列表
     */
    @Operation(summary = "获取内容的点赞列表", description = "获取指定内容的点赞用户列表")
    @GetMapping("/{contentId}/likes")
    @RequiresPermissions("content:action:query")
    public TableDataInfo getContentLikes(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        startPage();
        List<ContentActionVO> list = contentActionService.getContentLikes(contentId);
        return getDataTable(list);
    }

    /**
     * 获取内容的评论列表
     */
    @Operation(summary = "获取内容的评论列表", description = "获取指定内容的评论列表")
    @GetMapping("/{contentId}/comments")
    @RequiresPermissions("content:action:query")
    public TableDataInfo getContentComments(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        startPage();
        List<ContentActionVO> list = contentActionService.getContentComments(contentId);
        return getDataTable(list);
    }

    /**
     * 获取活动报名列表
     */
    @Operation(summary = "获取活动报名列表", description = "获取指定活动的报名用户列表")
    @GetMapping("/{activityId}/signups")
    @RequiresPermissions("content:action:query")
    public TableDataInfo getActivitySignups(
            @Parameter(description = "活动ID", required = true)
            @PathVariable Long activityId) {
        startPage();
        List<ContentActionVO> list = contentActionService.getActivitySignups(activityId);
        return getDataTable(list);
    }

    /**
     * 获取我的收藏列表
     */
    @Operation(summary = "获取我的收藏列表", description = "获取当前用户的收藏内容列表")
    @GetMapping("/my-collections")
    @RequiresPermissions("content:action:query")
    public TableDataInfo getMyCollections(
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer contentType) {
        startPage();
        List<ContentActionVO> list = contentActionService.getMyCollections(contentType);
        return getDataTable(list);
    }

    /**
     * 获取我的评论列表
     */
    @Operation(summary = "获取我的评论列表", description = "获取当前用户的评论记录")
    @GetMapping("/my-comments")
    @RequiresPermissions("content:action:query")
    public TableDataInfo getMyComments() {
        startPage();
        List<ContentActionVO> list = contentActionService.getMyComments();
        return getDataTable(list);
    }

    /**
     * 检查用户对内容的行为状态
     */
    @Operation(summary = "检查用户对内容的行为状态", description = "检查当前用户对指定内容的行为状态")
    @GetMapping("/{contentId}/status")
    @RequiresPermissions("content:action:query")
    public R<Map<String, Boolean>> checkActionStatus(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return R.ok(contentActionService.checkUserActionStatus(contentId));
    }

    /**
     * 获取内容行为统计
     */
    @Operation(summary = "获取内容行为统计", description = "获取指定内容的行为统计数据")
    @GetMapping("/{contentId}/statistics")
    @RequiresPermissions("content:action:query")
    public R<Map<String, Long>> getContentActionStatistics(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return R.ok(contentActionService.getContentActionStatistics(contentId));
    }

    /**
     * 举报内容
     */
    @Operation(summary = "举报内容", description = "举报不当内容")
    @PostMapping("/report")
    @RequiresPermissions("content:action:add")
    @Log(title = "举报内容", businessType = BusinessType.INSERT)
    public R<Void> reportContent(@Validated @RequestBody ContentActionAddDTO reportDTO) {
        return toAjax(contentActionService.reportContent(reportDTO));
    }

    /**
     * 删除行为记录
     */
    @Operation(summary = "删除行为记录", description = "删除指定的行为记录")
    @DeleteMapping("/{actionId}")
    @RequiresPermissions("content:action:remove")
    @Log(title = "删除行为记录", businessType = BusinessType.DELETE)
    public R<Void> deleteAction(
            @Parameter(description = "行为记录ID", required = true)
            @PathVariable Long actionId) {
        return toAjax(contentActionService.deleteAction(actionId));
    }
}
