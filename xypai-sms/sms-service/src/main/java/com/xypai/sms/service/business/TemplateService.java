package com.xypai.sms.service.business;

import com.xypai.sms.common.exception.BusinessException;
import com.xypai.sms.controller.dto.SmsTemplateDto;
import com.xypai.sms.controller.dto.TemplateQueryRequest;
import com.xypai.sms.service.repository.SmsTemplateRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business: 短信模板业务服务
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final SmsTemplateRepositoryService templateRepository;
    private final ValidationService validationService;

    /**
     * Business: 获取模板列表
     */
    public List<SmsTemplateDto> getTemplates(TemplateQueryRequest query) {
        log.info("Business: 查询模板列表, templateType={}, status={}, page={}",
                query.templateType(), query.status(), query.page());

        return templateRepository.findTemplates(query);
    }

    /**
     * Business: 获取模板详情
     */
    public SmsTemplateDto getTemplate(String templateCode) {
        log.info("Business: 查询模板详情, templateCode={}", templateCode);

        SmsTemplateDto template = templateRepository.findByTemplateCode(templateCode);
        if (template == null) {
            throw new BusinessException.TemplateNotFoundException(templateCode);
        }

        return template;
    }

    /**
     * Business: 创建模板
     */
    public SmsTemplateDto createTemplate(SmsTemplateDto templateDto) {
        log.info("Business: 创建模板, templateCode={}, templateName={}",
                templateDto.templateCode(), templateDto.templateName());

        try {
            // 1. 验证模板数据
            validationService.validateTemplate(templateDto);

            // 2. 检查模板代码是否已存在
            if (templateRepository.existsByTemplateCode(templateDto.templateCode())) {
                throw new BusinessException.TemplateAlreadyExistsException(templateDto.templateCode());
            }

            // 3. 保存模板
            SmsTemplateDto savedTemplate = templateRepository.save(templateDto);

            log.info("Business: 模板创建成功, templateCode={}, id={}",
                    savedTemplate.templateCode(), savedTemplate.id());

            return savedTemplate;

        } catch (BusinessException e) {
            log.error("Business: 模板创建业务异常, templateCode={}, error={}",
                    templateDto.templateCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Business: 模板创建系统异常, templateCode={}, error={}",
                    templateDto.templateCode(), e.getMessage(), e);
            throw new BusinessException("TEMPLATE_CREATE_ERROR", "模板创建失败: " + e.getMessage(), e);
        }
    }

    /**
     * Business: 审核通过模板
     */
    public void approveTemplate(String templateCode, String auditComment, String operator) {
        log.info("Business: 审核通过模板, templateCode={}, operator={}", templateCode, operator);

        try {
            // 1. 获取模板
            SmsTemplateDto template = getTemplate(templateCode);

            // 2. 检查状态
            if (!"PENDING_APPROVAL".equals(template.status())) {
                throw new BusinessException("INVALID_STATUS",
                        "只有待审核状态的模板才能审核通过，当前状态: " + template.status());
            }

            // 3. 更新状态
            templateRepository.updateStatus(templateCode, "ACTIVE", auditComment, operator);

            log.info("Business: 模板审核通过成功, templateCode={}", templateCode);

        } catch (BusinessException e) {
            log.error("Business: 模板审核业务异常, templateCode={}, error={}", templateCode, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Business: 模板审核系统异常, templateCode={}, error={}", templateCode, e.getMessage(), e);
            throw new BusinessException("TEMPLATE_APPROVE_ERROR", "模板审核失败: " + e.getMessage(), e);
        }
    }

    /**
     * Business: 审核拒绝模板
     */
    public void rejectTemplate(String templateCode, String auditComment, String operator) {
        log.info("Business: 审核拒绝模板, templateCode={}, operator={}", templateCode, operator);

        if (auditComment == null || auditComment.trim().isEmpty()) {
            throw new BusinessException("INVALID_PARAMS", "审核拒绝必须提供审核意见");
        }

        try {
            // 1. 获取模板
            SmsTemplateDto template = getTemplate(templateCode);

            // 2. 检查状态
            if (!"PENDING_APPROVAL".equals(template.status())) {
                throw new BusinessException("INVALID_STATUS",
                        "只有待审核状态的模板才能审核拒绝，当前状态: " + template.status());
            }

            // 3. 更新状态
            templateRepository.updateStatus(templateCode, "REJECTED", auditComment, operator);

            log.info("Business: 模板审核拒绝成功, templateCode={}", templateCode);

        } catch (BusinessException e) {
            log.error("Business: 模板审核业务异常, templateCode={}, error={}", templateCode, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Business: 模板审核系统异常, templateCode={}, error={}", templateCode, e.getMessage(), e);
            throw new BusinessException("TEMPLATE_REJECT_ERROR", "模板审核失败: " + e.getMessage(), e);
        }
    }
}
