package com.xypai.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 🏗️ 性别枚举 - 企业架构实现
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Getter
@AllArgsConstructor
public enum Gender {

    /**
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 男
     */
    MALE(1, "男"),

    /**
     * 女
     */
    FEMALE(2, "女"),

    /**
     * 其他
     */
    OTHER(3, "其他");

    private final Integer code;
    private final String desc;

    /**
     * 根据性别码获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知";
        }

        for (Gender gender : values()) {
            if (gender.getCode().equals(code)) {
                return gender.getDesc();
            }
        }
        return "未知";
    }
}
