package com.xypai.user.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xypai.user.domain.user.UserAggregate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户响应DTO
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Schema(description = "用户信息响应")
public record UserResponse(
        @Schema(description = "用户ID", example = "1")
        Long userId,

        @Schema(description = "手机号", example = "13888888888")
        String mobile,

        @Schema(description = "用户名", example = "testuser")
        String username,

        @Schema(description = "昵称", example = "测试用户")
        String nickname,

        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        String avatar,

        @Schema(description = "性别", example = "1")
        Integer gender,

        @Schema(description = "生日", example = "1990-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @Schema(description = "状态", example = "1")
        Integer status,

        @Schema(description = "客户端类型", example = "app")
        String clientType,

        @Schema(description = "真实姓名", example = "张三")
        String realName,

        @Schema(description = "邮箱", example = "test@example.com")
        String email,

        @Schema(description = "微信号", example = "wechat123")
        String wechat,

        @Schema(description = "职业", example = "软件工程师")
        String occupation,

        @Schema(description = "位置", example = "北京市")
        String location,

        @Schema(description = "个人简介", example = "这是个人简介")
        String bio,

        @Schema(description = "兴趣爱好", example = "编程,阅读,运动")
        String interests,

        @Schema(description = "身高", example = "175.5")
        BigDecimal height,

        @Schema(description = "体重", example = "70.0")
        BigDecimal weight,

        @Schema(description = "推送通知", example = "1")
        Integer notificationPush,

        @Schema(description = "隐私级别", example = "1")
        Integer privacyLevel,

        @Schema(description = "语言", example = "zh-CN")
        String language,

        @Schema(description = "注册时间", example = "2025-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime registerTime,

        @Schema(description = "最后登录时间", example = "2025-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastLoginTime,

        @Schema(description = "更新时间", example = "2025-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateTime
) {

    /**
     * 从聚合根创建响应对象
     */
    public static UserResponse from(UserAggregate aggregate) {
        return new UserResponse(
                aggregate.getUserId() != null ? aggregate.getUserId().value() : null,
                aggregate.getMobile(),
                aggregate.getUsername(),
                aggregate.getNickname(),
                aggregate.getAvatar(),
                aggregate.getGender(),
                aggregate.getBirthDate(),
                aggregate.getStatus(),
                aggregate.getClientType(),
                aggregate.getRealName(),
                aggregate.getEmail(),
                aggregate.getWechat(),
                aggregate.getOccupation(),
                aggregate.getLocation(),
                aggregate.getBio(),
                aggregate.getInterests(),
                aggregate.getHeight(),
                aggregate.getWeight(),
                aggregate.getNotificationPush(),
                aggregate.getPrivacyLevel(),
                aggregate.getLanguage(),
                aggregate.getRegisterTime(),
                aggregate.getLastLoginTime(),
                aggregate.getUpdateTime()
        );
    }
}
