package com.xypai.sms.controller.rest;

import com.xypai.sms.controller.dto.SmsTemplateDto;
import com.xypai.sms.controller.dto.TemplateQueryRequest;
import com.xypai.sms.service.business.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Template: 短信模板管理控制器
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@RestController
@RequestMapping("/sms/templates")
@RequiredArgsConstructor
@Tag(name = "短信模板管理", description = "短信模板的创建、查询、审核等管理功能")
public class SmsTemplateController {

    private final TemplateService templateService;

    /**
     * Template: 获取模板列表
     */
    @Operation(summary = "获取短信模板列表", description = "分页获取短信模板列表，支持按类型和状态筛选")
    @GetMapping
    public ResponseEntity<List<SmsTemplateDto>> getTemplates(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "模板类型", example = "VERIFICATION")
            @RequestParam(required = false) String templateType,
            @Parameter(description = "状态", example = "ACTIVE")
            @RequestParam(required = false) String status) {

        log.info("Template: 查询模板列表, page={}, size={}, templateType={}, status={}",
                page, size, templateType, status);

        TemplateQueryRequest query = new TemplateQueryRequest(templateType, status, page, size);
        List<SmsTemplateDto> templates = templateService.getTemplates(query);

        log.info("Template: 模板列表查询成功, count={}", templates.size());
        return ResponseEntity.ok(templates);
    }

    /**
     * Template: 获取模板详情
     */
    @Operation(summary = "获取模板详情", description = "根据模板编号获取模板详细信息")
    @GetMapping("/{templateCode}")
    public ResponseEntity<SmsTemplateDto> getTemplate(
            @Parameter(description = "模板编号", example = "USER_REGISTER_VERIFY", required = true)
            @PathVariable String templateCode) {

        log.info("Template: 查询模板详情, templateCode={}", templateCode);

        SmsTemplateDto template = templateService.getTemplate(templateCode);

        log.info("Template: 模板详情查询成功, templateCode={}", templateCode);
        return ResponseEntity.ok(template);
    }

    /**
     * Template: 创建模板
     */
    @Operation(summary = "创建短信模板", description = "创建新的短信模板，需要等待审核通过后才能使用")
    @PostMapping
    public ResponseEntity<SmsTemplateDto> createTemplate(
            @Valid @RequestBody SmsTemplateDto templateDto) {

        log.info("Template: 创建模板, templateCode={}, templateName={}",
                templateDto.templateCode(), templateDto.templateName());

        SmsTemplateDto response = templateService.createTemplate(templateDto);

        log.info("Template: 模板创建成功, templateCode={}", templateDto.templateCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Template: 审核通过模板
     */
    @Operation(summary = "审核通过模板", description = "审核通过指定的短信模板")
    @PostMapping("/{templateCode}/approve")
    public ResponseEntity<Void> approveTemplate(
            @Parameter(description = "模板编号", required = true)
            @PathVariable String templateCode,
            @Parameter(description = "审核意见")
            @RequestParam(required = false) String auditComment,
            @Parameter(description = "操作员")
            @RequestParam(defaultValue = "system") String operator) {

        log.info("Template: 审核通过模板, templateCode={}, operator={}", templateCode, operator);

        templateService.approveTemplate(templateCode, auditComment, operator);

        log.info("Template: 模板审核通过成功, templateCode={}", templateCode);
        return ResponseEntity.ok().build();
    }

    /**
     * Template: 审核拒绝模板
     */
    @Operation(summary = "审核拒绝模板", description = "审核拒绝指定的短信模板")
    @PostMapping("/{templateCode}/reject")
    public ResponseEntity<Void> rejectTemplate(
            @Parameter(description = "模板编号", required = true)
            @PathVariable String templateCode,
            @Parameter(description = "拒绝原因", required = true)
            @RequestParam String auditComment,
            @Parameter(description = "操作员")
            @RequestParam(defaultValue = "system") String operator) {

        log.info("Template: 审核拒绝模板, templateCode={}, operator={}", templateCode, operator);

        templateService.rejectTemplate(templateCode, auditComment, operator);

        log.info("Template: 模板审核拒绝成功, templateCode={}", templateCode);
        return ResponseEntity.ok().build();
    }
}
