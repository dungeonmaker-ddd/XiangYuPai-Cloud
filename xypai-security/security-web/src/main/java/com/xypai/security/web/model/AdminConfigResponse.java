package com.xypai.security.web.model;

import java.util.Map;
import java.util.Objects;

/**
 * 🏛️ 管理端配置响应 Record
 * <p>
 * XV03:02 管理端配置信息响应
 * 包含系统版本、权限配置等信息
 *
 * @author xypai
 * @since 1.0.0
 */
public record AdminConfigResponse(
        String version,
        String systemName,
        Map<String, Boolean> permissions
) {
    
    /**
     * 紧凑构造函数 - 数据验证
     */
    public AdminConfigResponse {
        Objects.requireNonNull(version, "版本号不能为空");
        Objects.requireNonNull(systemName, "系统名称不能为空");
        Objects.requireNonNull(permissions, "权限配置不能为空");
    }
    
    /**
     * 创建默认管理端配置
     */
    public static AdminConfigResponse createDefault() {
        Map<String, Boolean> defaultPermissions = Map.of(
            "userManagement", true,
            "roleManagement", true,
            "systemConfig", true,
            "auditLog", true,
            "statistics", true
        );
        
        return new AdminConfigResponse(
            "5.0.0",
            "相遇派管理系统",
            defaultPermissions
        );
    }
    
    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permissionKey) {
        return permissions.getOrDefault(permissionKey, false);
    }
}
