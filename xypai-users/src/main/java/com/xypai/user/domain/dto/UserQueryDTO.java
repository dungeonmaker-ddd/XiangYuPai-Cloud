package com.xypai.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

/**
 * 🏗️ 用户查询DTO - 企业架构实现
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Schema(description = "用户查询请求DTO")
public record UserQueryDTO(

        @Schema(description = "用户ID", example = "100000")
        Long userId,

        @Schema(description = "用户编码", example = "XY202501020001")
        String userCode,

        @Schema(description = "手机号", example = "139****0001")
        String mobile,

        @Schema(description = "用户名", example = "xypai_user")
        String username,

        @Schema(description = "用户昵称", example = "XY用户")
        String nickname,

        @Schema(description = "邮箱", example = "user@xypai.com")
        String email,

        @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2", "3"})
        @Min(value = 0, message = "性别值不能小于0")
        @Max(value = 3, message = "性别值不能大于3")
        Integer gender,

        @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1", "2", "3"})
        @Min(value = 0, message = "用户状态值不能小于0")
        @Max(value = 3, message = "用户状态值不能大于3")
        Integer status,

        @Schema(description = "用户类型", example = "1", allowableValues = {"0", "1", "2", "3"})
        @Min(value = 0, message = "用户类型值不能小于0")
        @Max(value = 3, message = "用户类型值不能大于3")
        Integer userType,

        @Schema(description = "是否实名认证", example = "1", allowableValues = {"0", "1"})
        @Min(value = 0, message = "实名认证状态值不能小于0")
        @Max(value = 1, message = "实名认证状态值不能大于1")
        Integer isVerified,

        @Schema(description = "注册平台", example = "iOS")
        String platform,

        @Schema(description = "注册来源渠道", example = "app_store")
        String sourceChannel,

        @Schema(description = "所在地区", example = "北京市")
        String location,

        @Schema(description = "部门ID", example = "100")
        Long deptId,

        @Schema(description = "用户等级最小值", example = "1")
        @Min(value = 1, message = "用户等级不能小于1")
        Integer minUserLevel,

        @Schema(description = "用户等级最大值", example = "10")
        @Max(value = 100, message = "用户等级不能大于100")
        Integer maxUserLevel,

        @Schema(description = "注册开始时间", example = "2025-01-01T00:00:00")
        LocalDateTime createTimeStart,

        @Schema(description = "注册结束时间", example = "2025-01-31T23:59:59")
        LocalDateTime createTimeEnd,

        @Schema(description = "最后登录开始时间", example = "2025-01-01T00:00:00")
        LocalDateTime lastLoginTimeStart,

        @Schema(description = "最后登录结束时间", example = "2025-01-31T23:59:59")
        LocalDateTime lastLoginTimeEnd

) {
}
