package com.xypai.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 🏗️ 用户状态枚举 - 企业架构实现
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 正常
     */
    NORMAL(1, "正常"),

    /**
     * 冻结
     */
    FROZEN(2, "冻结"),

    /**
     * 注销
     */
    CANCELLED(3, "注销");

    private final Integer code;
    private final String desc;

    /**
     * 根据状态码获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知";
        }

        for (UserStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知";
    }
}
