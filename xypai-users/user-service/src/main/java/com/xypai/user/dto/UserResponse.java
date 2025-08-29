package com.xypai.user.dto;

import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.entity.UserProfile;

import java.time.LocalDateTime;

/**
 * 👤 用户响应 - MVP版本
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public record UserResponse(
        Long id,
        String mobile,
        String username,
        String nickname,
        String avatar,
        Integer gender,
        String genderDesc,
        Integer status,
        String statusDesc,
        LocalDateTime createTime,

        // 扩展信息 (可选)
        String realName,
        String email,
        String bio
) {

    /**
     * 🔨 从用户实体创建响应
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getMobile(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getGender(),
                user.getGenderDesc(),
                user.getStatus(),
                user.getStatusDesc(),
                user.getCreateTime(),
                null, null, null
        );
    }

    /**
     * 🔨 从用户实体和扩展信息创建响应
     */
    public static UserResponse from(User user, UserProfile profile) {
        return new UserResponse(
                user.getId(),
                user.getMobile(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getGender(),
                user.getGenderDesc(),
                user.getStatus(),
                user.getStatusDesc(),
                user.getCreateTime(),
                profile != null ? profile.getRealName() : null,
                profile != null ? profile.getEmail() : null,
                profile != null ? profile.getBio() : null
        );
    }
}
