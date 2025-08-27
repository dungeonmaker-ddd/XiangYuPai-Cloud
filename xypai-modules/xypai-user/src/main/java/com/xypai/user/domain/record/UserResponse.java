package com.xypai.user.domain.record;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户响应记录
 *
 * @author XyPai
 */
public record UserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        Integer gender,
        @JsonProperty("dept_name")
        String deptName,
        String avatar,
        Integer status,
        String remark,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateTime
) {
    public UserResponse {
        Objects.requireNonNull(id, "用户ID不能为null");
        Objects.requireNonNull(username, "用户名不能为null");
        Objects.requireNonNull(nickname, "昵称不能为null");
    }

    /**
     * 创建基础用户响应
     */
    public static UserResponse of(Long id, String username, String nickname, String email,
                                  String phone, Integer gender, Integer status) {
        return new UserResponse(
                id, username, nickname, email, phone, gender,
                null, null, status, null, null, null
        );
    }

    /**
     * 创建完整用户响应
     */
    public static UserResponse full(Long id, String username, String nickname, String email,
                                    String phone, Integer gender, String deptName, String avatar,
                                    Integer status, String remark, LocalDateTime createTime,
                                    LocalDateTime updateTime) {
        return new UserResponse(
                id, username, nickname, email, phone, gender,
                deptName, avatar, status, remark, createTime, updateTime
        );
    }

    /**
     * 格式化性别显示
     */
    public String genderDisplay() {
        return switch (gender) {
            case 0 -> "男";
            case 1 -> "女";
            case 2 -> "未知";
            default -> "未知";
        };
    }

    /**
     * 格式化状态显示
     */
    public String statusDisplay() {
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "停用";
            default -> "未知";
        };
    }

    /**
     * 是否为活跃用户
     */
    public boolean isActive() {
        return status != null && status == 0;
    }
}
