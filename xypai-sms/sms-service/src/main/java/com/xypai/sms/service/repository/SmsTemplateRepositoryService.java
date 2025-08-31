package com.xypai.sms.service.repository;

import com.xypai.sms.controller.dto.SmsTemplateDto;
import com.xypai.sms.controller.dto.TemplateQueryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repository: 短信模板仓储服务 (第一版：内存模拟实现)
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Service
public class SmsTemplateRepositoryService {

    // 第一版：使用内存存储模拟数据库
    private final ConcurrentHashMap<String, SmsTemplateDto> templateStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public SmsTemplateRepositoryService() {
        // 初始化测试数据
        initTestData();
    }

    /**
     * 初始化测试数据
     */
    private void initTestData() {
        // 验证码模板
        SmsTemplateDto verifyTemplate = new SmsTemplateDto(
                1L,
                "USER_REGISTER_VERIFY",
                "用户注册验证码",
                "您的验证码是：${code}，有效期5分钟，请勿泄露给他人。",
                "VERIFICATION",
                java.util.Set.of("ALIYUN"),
                java.util.Map.of("code", "验证码"),
                "ACTIVE",
                "审核通过",
                "验证码模板",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(9)
        );
        templateStore.put("USER_REGISTER_VERIFY", verifyTemplate);

        // 登录通知模板
        SmsTemplateDto loginTemplate = new SmsTemplateDto(
                2L,
                "USER_LOGIN_NOTIFY",
                "登录通知",
                "您的账户于${time}在${location}登录，如非本人操作请及时修改密码。",
                "NOTIFICATION",
                java.util.Set.of("ALIYUN", "TENCENT"),
                java.util.Map.of("time", "登录时间", "location", "登录地点"),
                "ACTIVE",
                "审核通过",
                "登录通知模板",
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(4)
        );
        templateStore.put("USER_LOGIN_NOTIFY", loginTemplate);

        // 密码重置模板
        SmsTemplateDto resetTemplate = new SmsTemplateDto(
                3L,
                "PASSWORD_RESET",
                "密码重置验证码",
                "您的密码重置验证码是：${code}，有效期10分钟。如非本人操作请忽略。",
                "VERIFICATION",
                java.util.Set.of("TENCENT"),
                java.util.Map.of("code", "验证码"),
                "PENDING",
                null,
                "密码重置模板",
                LocalDateTime.now().minusDays(1),
                null
        );
        templateStore.put("PASSWORD_RESET", resetTemplate);

        log.info("Repository: 初始化测试数据完成, 模板数量={}", templateStore.size());
    }

    /**
     * Repository: 根据模板编号查找模板
     */
    public SmsTemplateDto findByTemplateCode(String templateCode) {
        log.debug("Repository: 查找模板, templateCode={}", templateCode);

        SmsTemplateDto template = templateStore.get(templateCode);
        if (template != null) {
            log.debug("Repository: 找到模板, templateCode={}", templateCode);
        } else {
            log.warn("Repository: 模板不存在, templateCode={}", templateCode);
        }

        return template;
    }

    /**
     * Repository: 检查模板编号是否存在
     */
    public boolean existsByTemplateCode(String templateCode) {
        log.debug("Repository: 检查模板是否存在, templateCode={}", templateCode);
        return templateStore.containsKey(templateCode);
    }

    /**
     * Repository: 保存模板
     */
    public SmsTemplateDto save(SmsTemplateDto template) {
        log.info("Repository: 保存模板, templateCode={}", template.templateCode());

        // 如果是新模板，分配ID
        SmsTemplateDto savedTemplate = template;
        if (template.id() == null) {
            savedTemplate = new SmsTemplateDto(
                    idGenerator.getAndIncrement(),
                    template.templateCode(),
                    template.templateName(),
                    template.content(),
                    template.templateType(),
                    template.supportedChannels(),
                    template.parameterConfig(),
                    template.status(),
                    template.auditComment(),
                    template.remark(),
                    template.createdAt(),
                    template.updatedAt()
            );
        }

        templateStore.put(template.templateCode(), savedTemplate);
        log.info("Repository: 模板保存成功, templateCode={}, id={}",
                savedTemplate.templateCode(), savedTemplate.id());

        return savedTemplate;
    }

    /**
     * Repository: 分页查询模板列表
     */
    public List<SmsTemplateDto> findTemplates(TemplateQueryRequest query) {
        log.info("Repository: 分页查询模板, templateType={}, status={}, page={}, size={}",
                query.templateType(), query.status(), query.page(), query.size());

        return templateStore.values().stream()
                .filter(template -> {
                    // 按类型过滤
                    if (query.templateType() != null && !query.templateType().isEmpty()) {
                        return template.templateType().equals(query.templateType());
                    }
                    return true;
                })
                .filter(template -> {
                    // 按状态过滤
                    if (query.status() != null && !query.status().isEmpty()) {
                        return template.status().equals(query.status());
                    }
                    return true;
                })
                .skip((long) (query.page() - 1) * query.size())
                .limit(query.size())
                .collect(Collectors.toList());
    }

    /**
     * Repository: 更新模板状态
     */
    public void updateStatus(String templateCode, String status, String auditComment, String operator) {
        log.info("Repository: 更新模板状态, templateCode={}, status={}", templateCode, status);

        SmsTemplateDto existingTemplate = templateStore.get(templateCode);
        if (existingTemplate != null) {
            SmsTemplateDto updatedTemplate = new SmsTemplateDto(
                    existingTemplate.id(),
                    existingTemplate.templateCode(),
                    existingTemplate.templateName(),
                    existingTemplate.content(),
                    existingTemplate.templateType(),
                    existingTemplate.supportedChannels(),
                    existingTemplate.parameterConfig(),
                    status,
                    auditComment,
                    existingTemplate.remark(),
                    existingTemplate.createdAt(),
                    LocalDateTime.now() // 更新时间
            );

            templateStore.put(templateCode, updatedTemplate);
            log.info("Repository: 模板状态更新成功, templateCode={}, status={}", templateCode, status);
        } else {
            log.warn("Repository: 模板不存在，无法更新状态, templateCode={}", templateCode);
        }
    }
}