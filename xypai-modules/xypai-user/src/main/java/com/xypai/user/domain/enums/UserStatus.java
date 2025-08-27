package com.xypai.user.domain.enums;

/**
 * 用户状态枚举
 *
 * @author XyPai
 */
public enum UserStatus {

    /**
     * 正常状态
     */
    NORMAL(0, "正常"),

    /**
     * 停用状态
     */
    DISABLED(1, "停用");

    private final Integer code;
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static UserStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的用户状态代码: " + code);
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 是否为正常状态
     */
    public boolean isNormal() {
        return this == NORMAL;
    }

    /**
     * 是否为停用状态
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
}
