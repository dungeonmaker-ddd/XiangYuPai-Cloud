package com.xypai.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 🏗️ 用户创建DTO - 企业架构实现
 * <p>
 * 遵循企业微服务架构规范：
 * - 使用Record实现不可变对象
 * - 完整的校验注解
 * - API文档注解
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Schema(description = "用户创建请求DTO")
public record UserAddDTO(

        @Schema(description = "手机号", example = "13900000001", required = true)
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String mobile,

        @Schema(description = "用户名", example = "xypai_user001", required = true)
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        String username,

        @Schema(description = "用户昵称", example = "XY用户", required = true)
        @NotBlank(message = "用户昵称不能为空")
        @Size(max = 100, message = "用户昵称长度不能超过100字符")
        String nickname,

        @Schema(description = "邮箱", example = "user@xypai.com")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100字符")
        String email,

        @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2", "3"})
        @Min(value = 0, message = "性别值不能小于0")
        @Max(value = 3, message = "性别值不能大于3")
        Integer gender,

        @Schema(description = "所在地区", example = "北京市")
        @Size(max = 200, message = "所在地区长度不能超过200字符")
        String location,

        @Schema(description = "注册平台", example = "iOS", allowableValues = {"iOS", "Android", "Web", "WeChat"})
        @Size(max = 50, message = "注册平台长度不能超过50字符")
        String platform,

        @Schema(description = "注册来源渠道", example = "app_store")
        @Size(max = 100, message = "注册来源渠道长度不能超过100字符")
        String sourceChannel,

        @Schema(description = "个人简介", example = "XY相遇派新用户")
        @Size(max = 500, message = "个人简介长度不能超过500字符")
        String bio,

        @Schema(description = "部门ID", example = "100")
        Long deptId

) {
}
