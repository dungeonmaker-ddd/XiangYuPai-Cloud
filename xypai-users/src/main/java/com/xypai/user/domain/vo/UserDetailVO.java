package com.xypai.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 🏗️ 用户详情VO - 企业架构实现
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Schema(description = "用户详情响应VO")
@Builder
public record UserDetailVO(

        @Schema(description = "用户ID", example = "100000")
        Long userId,

        @Schema(description = "用户编码", example = "XY202501020001")
        String userCode,

        @Schema(description = "手机号", example = "139****0001")
        String mobile,

        @Schema(description = "用户名", example = "xypai_user001")
        String username,

        @Schema(description = "用户昵称", example = "XY用户")
        String nickname,

        @Schema(description = "邮箱", example = "u***@xypai.com")
        String email,

        @Schema(description = "真实姓名", example = "*明")
        String realName,

        @Schema(description = "性别", example = "1")
        Integer gender,

        @Schema(description = "性别描述", example = "男")
        String genderDesc,

        @Schema(description = "头像URL", example = "https://cdn.xypai.com/avatar/default.jpg")
        String avatarUrl,

        @Schema(description = "生日", example = "1990-01-01T00:00:00")
        LocalDateTime birthday,

        @Schema(description = "所在地区", example = "北京市")
        String location,

        @Schema(description = "个人简介", example = "XY相遇派用户")
        String bio,

        @Schema(description = "用户状态", example = "1")
        Integer status,

        @Schema(description = "用户状态描述", example = "正常")
        String statusDesc,

        @Schema(description = "用户类型", example = "1")
        Integer userType,

        @Schema(description = "用户类型描述", example = "VIP用户")
        String userTypeDesc,

        @Schema(description = "是否实名认证", example = "1")
        Integer isVerified,

        @Schema(description = "实名认证描述", example = "已认证")
        String verifiedDesc,

        @Schema(description = "注册平台", example = "iOS")
        String platform,

        @Schema(description = "注册来源渠道", example = "app_store")
        String sourceChannel,

        @Schema(description = "用户等级", example = "5")
        Integer userLevel,

        @Schema(description = "用户积分", example = "1500")
        Integer userPoints,

        @Schema(description = "用户余额(分)", example = "10000")
        Long balance,

        @Schema(description = "登录次数", example = "25")
        Integer loginCount,

        @Schema(description = "最后登录时间", example = "2025-01-02T10:30:00")
        LocalDateTime lastLoginTime,

        @Schema(description = "最后登录IP", example = "192.168.1.100")
        String lastLoginIp,

        @Schema(description = "注册时间", example = "2024-12-01T09:00:00")
        LocalDateTime createTime,

        @Schema(description = "最后更新时间", example = "2025-01-02T10:30:00")
        LocalDateTime updateTime,

        @Schema(description = "版本号", example = "1")
        Integer version

) {
}
