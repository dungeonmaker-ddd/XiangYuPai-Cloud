package com.xypai.user.domain.enums;

/**
 * 用户性别枚举
 *
 * @author XyPai
 */
public enum UserGender {

    /**
     * 男性
     */
    MALE(0, "男"),

    /**
     * 女性
     */
    FEMALE(1, "女"),

    /**
     * 未知
     */
    UNKNOWN(2, "未知");

    private final Integer code;
    private final String description;

    UserGender(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static UserGender of(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (UserGender gender : values()) {
            if (gender.code.equals(code)) {
                return gender;
            }
        }
        return UNKNOWN;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 是否为男性
     */
    public boolean isMale() {
        return this == MALE;
    }

    /**
     * 是否为女性
     */
    public boolean isFemale() {
        return this == FEMALE;
    }

    /**
     * 是否为未知
     */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }
}
