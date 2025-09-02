package com.xypai.content.controller.app;

import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.core.web.page.TableDataInfo;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.security.annotation.RequiresPermissions;
import com.xypai.content.domain.dto.ContentAddDTO;
import com.xypai.content.domain.dto.ContentQueryDTO;
import com.xypai.content.domain.dto.ContentUpdateDTO;
import com.xypai.content.domain.vo.ContentDetailVO;
import com.xypai.content.domain.vo.ContentListVO;
import com.xypai.content.service.IContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 内容管理控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "内容管理", description = "内容发布与管理API")
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
@Validated
public class ContentController extends BaseController {

    private final IContentService contentService;

    /**
     * 查询内容列表
     */
    @Operation(summary = "查询内容列表", description = "分页查询内容列表信息")
    @GetMapping("/list")
    @RequiresPermissions("content:content:list")
    public TableDataInfo list(ContentQueryDTO query) {
        startPage();
        List<ContentListVO> list = contentService.selectContentList(query);
        return getDataTable(list);
    }

    /**
     * 获取内容详细信息
     */
    @Operation(summary = "获取内容详细信息", description = "根据内容ID获取详细信息")
    @GetMapping("/{contentId}")
    @RequiresPermissions("content:content:query")
    @Log(title = "内容管理", businessType = BusinessType.QUERY)
    public R<ContentDetailVO> getInfo(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return R.ok(contentService.selectContentById(contentId));
    }

    /**
     * 发布内容
     */
    @Operation(summary = "发布内容", description = "创建新内容")
    @PostMapping
    @RequiresPermissions("content:content:add")
    @Log(title = "内容管理", businessType = BusinessType.INSERT)
    public R<Void> add(@Validated @RequestBody ContentAddDTO contentAddDTO) {
        return contentService.insertContent(contentAddDTO) ? R.ok() : R.fail();
    }

    /**
     * 修改内容
     */
    @Operation(summary = "修改内容", description = "更新内容信息")
    @PutMapping
    @RequiresPermissions("content:content:edit")
    @Log(title = "内容管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@Validated @RequestBody ContentUpdateDTO contentUpdateDTO) {
        return contentService.updateContent(contentUpdateDTO) ? R.ok() : R.fail();
    }

    /**
     * 删除内容
     */
    @Operation(summary = "删除内容", description = "根据内容ID删除内容")
    @DeleteMapping("/{contentIds}")
    @RequiresPermissions("content:content:remove")
    @Log(title = "内容管理", businessType = BusinessType.DELETE)
    public R<Void> remove(
            @Parameter(description = "内容ID数组", required = true)
            @PathVariable Long[] contentIds) {
        return contentService.deleteContentByIds(Arrays.asList(contentIds)) ? R.ok() : R.fail();
    }

    /**
     * 发布内容
     */
    @Operation(summary = "发布内容", description = "将草稿内容发布")
    @PutMapping("/{contentId}/publish")
    @RequiresPermissions("content:content:edit")
    @Log(title = "发布内容", businessType = BusinessType.UPDATE)
    public R<Void> publishContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return contentService.publishContent(contentId) ? R.ok() : R.fail();
    }

    /**
     * 下架内容
     */
    @Operation(summary = "下架内容", description = "将已发布内容下架")
    @PutMapping("/{contentId}/archive")
    @RequiresPermissions("content:content:edit")
    @Log(title = "下架内容", businessType = BusinessType.UPDATE)
    public R<Void> archiveContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return contentService.archiveContent(contentId) ? R.ok() : R.fail();
    }

    /**
     * 获取热门内容
     */
    @Operation(summary = "获取热门内容", description = "获取热门内容列表")
    @GetMapping("/hot")
    @RequiresPermissions("content:content:query")
    public TableDataInfo getHotContents(
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        startPage();
        List<ContentListVO> list = contentService.getHotContents(type, limit);
        return getDataTable(list);
    }

    /**
     * 获取推荐内容
     */
    @Operation(summary = "获取推荐内容", description = "获取个性化推荐内容")
    @GetMapping("/recommended")
    @RequiresPermissions("content:content:query")
    public TableDataInfo getRecommendedContents(
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "20") Integer limit) {
        startPage();
        List<ContentListVO> list = contentService.getRecommendedContents(type, limit);
        return getDataTable(list);
    }

    /**
     * 搜索内容
     */
    @Operation(summary = "搜索内容", description = "根据关键词搜索内容")
    @GetMapping("/search")
    @RequiresPermissions("content:content:query")
    public TableDataInfo searchContents(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type) {
        startPage();
        List<ContentListVO> list = contentService.searchContents(keyword, type);
        return getDataTable(list);
    }

    /**
     * 获取用户内容
     */
    @Operation(summary = "获取用户内容", description = "获取指定用户发布的内容")
    @GetMapping("/user/{userId}")
    @RequiresPermissions("content:content:query")
    public TableDataInfo getUserContents(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type) {
        startPage();
        List<ContentListVO> list = contentService.getUserContents(userId, type);
        return getDataTable(list);
    }

    /**
     * 获取我的内容
     */
    @Operation(summary = "获取我的内容", description = "获取当前用户的内容列表")
    @GetMapping("/my")
    @RequiresPermissions("content:content:query")
    public TableDataInfo getMyContents(
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "内容状态")
            @RequestParam(required = false) Integer status) {
        startPage();
        List<ContentListVO> list = contentService.getMyContents(type, status);
        return getDataTable(list);
    }

    /**
     * 获取内容统计
     */
    @Operation(summary = "获取内容统计", description = "获取内容统计数据")
    @GetMapping("/statistics")
    @RequiresPermissions("content:content:query")
    public R<Map<String, Object>> getContentStatistics(
            @Parameter(description = "统计开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "统计结束时间")
            @RequestParam(required = false) String endDate) {
        return R.ok(contentService.getContentStatistics(startDate, endDate));
    }

    /**
     * 获取内容类型统计
     */
    @Operation(summary = "获取内容类型统计", description = "获取各类型内容数量统计")
    @GetMapping("/type-statistics")
    @RequiresPermissions("content:content:query")
    public R<Map<String, Object>> getContentTypeStatistics() {
        return R.ok(contentService.getContentTypeStatistics());
    }

    /**
     * 批量发布内容
     */
    @Operation(summary = "批量发布内容", description = "批量发布多个内容")
    @PutMapping("/batch-publish")
    @RequiresPermissions("content:content:edit")
    @Log(title = "批量发布内容", businessType = BusinessType.UPDATE)
    public R<Void> batchPublishContents(
            @Parameter(description = "内容ID列表", required = true)
            @RequestBody List<Long> contentIds) {
        return contentService.batchPublishContents(contentIds) ? R.ok() : R.fail();
    }

    /**
     * 批量下架内容
     */
    @Operation(summary = "批量下架内容", description = "批量下架多个内容")
    @PutMapping("/batch-archive")
    @RequiresPermissions("content:content:edit")
    @Log(title = "批量下架内容", businessType = BusinessType.UPDATE)
    public R<Void> batchArchiveContents(
            @Parameter(description = "内容ID列表", required = true)
            @RequestBody List<Long> contentIds) {
        return contentService.batchArchiveContents(contentIds) ? R.ok() : R.fail();
    }
}
