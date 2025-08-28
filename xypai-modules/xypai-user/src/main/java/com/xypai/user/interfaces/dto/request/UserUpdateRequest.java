package com.xypai.user.interfaces.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 用户更新请求（增强单表版本）
 * 支持Builder模式，使用更加灵活
 *
 * @author XyPai
 */
@Builder
public record UserUpdateRequest(
        @NotNull(message = "用户ID不能为空")
        Long userId,

        // 基础信息
        @Size(max = 30, message = "用户名不能超过30个字符")
        String username,

        @Size(max = 30, message = "昵称不能超过30个字符")
        String nickname,

        @Size(max = 200, message = "头像URL不能超过200个字符")
        String avatar,

        @Min(value = 0, message = "性别值必须为0、1或2")
        @Max(value = 2, message = "性别值必须为0、1或2")
        Integer gender,

        LocalDate birthDate,

        // 详细资料
        @Size(max = 50, message = "真实姓名不能超过50个字符")
        String realName,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过100个字符")
        String email,

        @Size(max = 50, message = "微信号不能超过50个字符")
        String wechat,

        @Size(max = 50, message = "职业不能超过50个字符")
        String occupation,

        @Size(max = 100, message = "常居地不能超过100个字符")
        String location,

        @Size(max = 500, message = "个人介绍不能超过500个字符")
        String bio,

        @Size(max = 200, message = "兴趣爱好不能超过200个字符")
        String interests,

        @DecimalMin(value = "50.0", message = "身高不能小于50cm")
        @DecimalMax(value = "300.0", message = "身高不能大于300cm")
        BigDecimal height,

        @DecimalMin(value = "20.0", message = "体重不能小于20kg")
        @DecimalMax(value = "500.0", message = "体重不能大于500kg")
        BigDecimal weight,

        // 设置信息
        @Min(value = 0, message = "推送通知设置值只能是0或1")
        @Max(value = 1, message = "推送通知设置值只能是0或1")
        Integer notificationPush,

        @Min(value = 1, message = "隐私级别值无效")
        @Max(value = 3, message = "隐私级别值无效")
        Integer privacyLevel,

        @Size(max = 10, message = "语言代码不能超过10个字符")
        String language
) {
    public UserUpdateRequest {
        Objects.requireNonNull(userId, "用户ID不能为null");

        // 验证生日不能是未来日期
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("生日不能是未来日期");
        }
    }

    // ========================================
    // 现代化工厂方法（使用Builder模式）
    // ========================================

    /**
     * 仅更新昵称
     */
    public static UserUpdateRequest updateNickname(Long userId, String nickname) {
        return UserUpdateRequest.builder()
                .userId(userId)
                .nickname(nickname)
                .build();
    }

    /**
     * 仅更新头像
     */
    public static UserUpdateRequest updateAvatar(Long userId, String avatar) {
        return UserUpdateRequest.builder()
                .userId(userId)
                .avatar(avatar)
                .build();
    }

    /**
     * 更新基础信息
     */
    public static UserUpdateRequest updateBasic(Long userId, String nickname, Integer gender) {
        return UserUpdateRequest.builder()
                .userId(userId)
                .nickname(nickname)
                .gender(gender)
                .build();
    }

    /**
     * 更新联系信息
     */
    public static UserUpdateRequest updateContact(Long userId, String email, String wechat) {
        return UserUpdateRequest.builder()
                .userId(userId)
                .email(email)
                .wechat(wechat)
                .build();
    }

    /**
     * 更新个人资料
     */
    public static UserUpdateRequest updateProfile(Long userId, String realName, String occupation, String location) {
        return UserUpdateRequest.builder()
                .userId(userId)
                .realName(realName)
                .occupation(occupation)
                .location(location)
                .build();
    }

    /**
     * 更新身体信息
     */
    public static UserUpdateRequest updatePhysical(Long userId, BigDecimal height, BigDecimal weight) {
        return UserUpdateRequest.builder()
                .userId(userId)
                .height(height)
                .weight(weight)
                .build();
    }
}
