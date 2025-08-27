package com.xypai.user.utils;

import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.record.UserCreateRequest;
import com.xypai.user.domain.record.UserResponse;
import com.xypai.user.domain.record.UserUpdateRequest;

import java.util.List;
import java.util.Objects;

/**
 * 用户对象转换器工具类
 *
 * @author XyPai
 */
public final class UserConverter {

    private UserConverter() {
        // 工具类禁止实例化
    }

    /**
     * 创建请求转实体
     */
    public static User toEntity(UserCreateRequest request) {
        Objects.requireNonNull(request, "用户创建请求不能为null");

        User user = new User();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setGender(request.gender());
        user.setStatus(0); // 默认正常状态
        user.setDelFlag(0); // 默认未删除

        return user;
    }

    /**
     * 更新请求应用到实体
     */
    public static void applyUpdateToEntity(UserUpdateRequest request, User user) {
        Objects.requireNonNull(request, "用户更新请求不能为null");
        Objects.requireNonNull(user, "用户实体不能为null");

        request.nickname().ifPresent(user::setNickname);
        request.email().ifPresent(user::setEmail);
        request.phone().ifPresent(user::setPhone);
        request.gender().ifPresent(user::setGender);
        request.status().ifPresent(user::setStatus);
    }

    /**
     * 实体转响应对象
     */
    public static UserResponse toResponse(User user) {
        Objects.requireNonNull(user, "用户实体不能为null");

        return UserResponse.full(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getGender(),
                null, // deptName - 已从实体移除
                null, // avatar - 已从实体移除
                user.getStatus(),
                null, // remark - 已从实体移除
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    /**
     * 实体列表转响应对象列表
     */
    public static List<UserResponse> toResponseList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        return users.stream()
                .map(UserConverter::toResponse)
                .toList();
    }

    /**
     * 创建基础用户响应（不包含敏感信息）
     */
    public static UserResponse toBasicResponse(User user) {
        Objects.requireNonNull(user, "用户实体不能为null");

        return UserResponse.of(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                null, // 不包含邮箱
                null, // 不包含手机号
                user.getGender(),
                user.getStatus()
        );
    }

    /**
     * 实体列表转基础响应对象列表
     */
    public static List<UserResponse> toBasicResponseList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        return users.stream()
                .map(UserConverter::toBasicResponse)
                .toList();
    }
}
