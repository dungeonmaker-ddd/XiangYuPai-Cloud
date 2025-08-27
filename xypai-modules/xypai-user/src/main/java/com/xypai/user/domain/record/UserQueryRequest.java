package com.xypai.user.domain.record;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Optional;

/**
 * 用户查询请求记录
 *
 * @author XyPai
 */
public record UserQueryRequest(
        @Size(max = 20, message = "用户名长度不能超过20个字符")
        Optional<String> username,

        @Size(max = 30, message = "昵称长度不能超过30个字符")
        Optional<String> nickname,

        @Size(max = 50, message = "邮箱长度不能超过50个字符")
        Optional<String> email,

        Optional<String> phone,

        @Min(value = 0, message = "性别值无效")
        @Max(value = 2, message = "性别值无效")
        Optional<Integer> gender,

        @Size(max = 30, message = "部门名称长度不能超过30个字符")
        Optional<String> deptName,

        @Min(value = 0, message = "状态值无效")
        @Max(value = 1, message = "状态值无效")
        Optional<Integer> status,

        @Min(value = 1, message = "页码必须大于0")
        Optional<Integer> pageNum,

        @Min(value = 1, message = "每页大小必须大于0")
        @Max(value = 100, message = "每页大小不能超过100")
        Optional<Integer> pageSize
) {
    public UserQueryRequest {
        // 验证字符串字段不为空字符串
        username.ifPresent(value -> {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("用户名不能为空字符串");
            }
        });

        nickname.ifPresent(value -> {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("昵称不能为空字符串");
            }
        });

        email.ifPresent(value -> {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("邮箱不能为空字符串");
            }
        });

        phone.ifPresent(value -> {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("手机号不能为空字符串");
            }
        });

        deptName.ifPresent(value -> {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("部门名称不能为空字符串");
            }
        });
    }

    /**
     * 创建空查询请求（查询所有）
     */
    public static UserQueryRequest all() {
        return new UserQueryRequest(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(1), Optional.of(10)
        );
    }

    /**
     * 按用户名查询
     */
    public static UserQueryRequest byUsername(String username) {
        return new UserQueryRequest(
                Optional.of(username), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(1), Optional.of(10)
        );
    }

    /**
     * 按状态查询
     */
    public static UserQueryRequest byStatus(Integer status) {
        return new UserQueryRequest(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(status),
                Optional.of(1), Optional.of(10)
        );
    }

    /**
     * 按部门查询
     */
    public static UserQueryRequest byDept(String deptName) {
        return new UserQueryRequest(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(deptName), Optional.empty(),
                Optional.of(1), Optional.of(10)
        );
    }

    /**
     * 设置分页参数
     */
    public UserQueryRequest withPagination(int pageNum, int pageSize) {
        return new UserQueryRequest(
                username, nickname, email, phone, gender, deptName, status,
                Optional.of(pageNum), Optional.of(pageSize)
        );
    }

    /**
     * 获取页码，默认为1
     */
    public int getPageNum() {
        return pageNum.orElse(1);
    }

    /**
     * 获取每页大小，默认为10
     */
    public int getPageSize() {
        return pageSize.orElse(10);
    }
}
