package com.xypai.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 🏗️ 用户更新DTO - 企业架构实现
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Schema(description = "用户更新请求DTO")
public record UserUpdateDTO(

        @Schema(description = "用户ID", required = true, example = "100000")
        @NotNull(message = "用户ID不能为空")
        Long userId,

        @Schema(description = "用户昵称", example = "新昵称")
        @Size(max = 100, message = "用户昵称长度不能超过100字符")
        String nickname,

        @Schema(description = "邮箱", example = "newemail@xypai.com")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100字符")
        String email,

        @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2", "3"})
        @Min(value = 0, message = "性别值不能小于0")
        @Max(value = 3, message = "性别值不能大于3")
        Integer gender,

        @Schema(description = "头像URL", example = "https://cdn.xypai.com/avatar/new.jpg")
        @Size(max = 500, message = "头像URL长度不能超过500字符")
        String avatarUrl,

        @Schema(description = "所在地区", example = "上海市")
        @Size(max = 200, message = "所在地区长度不能超过200字符")
        String location,

        @Schema(description = "个人简介", example = "更新后的个人简介")
        @Size(max = 500, message = "个人简介长度不能超过500字符")
        String bio,

        @Schema(description = "版本号", required = true, example = "1")
        @NotNull(message = "版本号不能为空")
        Integer version

) {
}
