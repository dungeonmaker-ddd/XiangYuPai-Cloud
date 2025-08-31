package com.xypai.sms.controller.dto;

/**
 * DTO: 模板查询请求
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public record TemplateQueryRequest(
        String templateType,
        String status,
        Integer page,
        Integer size
) {

    /**
     * DTO: 紧凑构造器验证
     */
    public TemplateQueryRequest {
        if (page == null || page < 1) {
            page = 1;
        }

        if (size == null || size < 1) {
            size = 20;
        }

        if (size > 100) {
            size = 100;
        }
    }

    /**
     * DTO: 创建查询请求
     */
    public static TemplateQueryRequest of(String templateType, String status) {
        return new TemplateQueryRequest(templateType, status, 1, 20);
    }

    /**
     * DTO: 创建分页查询请求
     */
    public static TemplateQueryRequest ofPage(Integer page, Integer size) {
        return new TemplateQueryRequest(null, null, page, size);
    }

    /**
     * DTO: 计算偏移量
     */
    public int getOffset() {
        return (page - 1) * size;
    }

    /**
     * DTO: 检查是否有类型过滤
     */
    public boolean hasTypeFilter() {
        return templateType != null && !templateType.trim().isEmpty();
    }

    /**
     * DTO: 检查是否有状态过滤
     */
    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty();
    }
}
