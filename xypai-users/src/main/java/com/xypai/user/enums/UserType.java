package com.xypai.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 🏗️ 用户类型枚举 - 企业架构实现
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Getter
@AllArgsConstructor
public enum UserType {

    /**
     * 普通用户
     */
    NORMAL(0, "普通用户"),

    /**
     * VIP用户
     */
    VIP(1, "VIP用户"),

    /**
     * SVIP用户
     */
    SVIP(2, "SVIP用户"),

    /**
     * 企业用户
     */
    ENTERPRISE(3, "企业用户");

    private final Integer code;
    private final String desc;

    /**
     * 根据类型码获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知";
        }

        for (UserType type : values()) {
            if (type.getCode().equals(code)) {
                return type.getDesc();
            }
        }
        return "未知";
    }
}
