package com.xypai.user.interfaces.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 用户查询请求
 *
 * @author XyPai
 */
public record UserQueryRequest(
        @Min(value = 1, message = "页码必须大于0")
        Integer pageNum,

        @Min(value = 1, message = "每页大小必须大于0")
        @Max(value = 100, message = "每页大小不能超过100")
        Integer pageSize,

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String mobile,

        @Size(max = 30, message = "用户名不能超过30个字符")
        String username,

        @Size(max = 30, message = "昵称不能超过30个字符")
        String nickname,

        @Min(value = 0, message = "性别值必须为0、1或2")
        @Max(value = 2, message = "性别值必须为0、1或2")
        Integer gender,

        @Min(value = 0, message = "状态值必须为0或1")
        @Max(value = 1, message = "状态值必须为0或1")
        Integer status,

        LocalDate birthStartDate,

        LocalDate birthEndDate,

        @Pattern(regexp = "^(web|app|mini)$", message = "客户端类型只能是web、app或mini")
        String clientType
) {
    public UserQueryRequest {
        // 设置默认分页参数
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 20;
        }

        // 验证日期范围
        if (birthStartDate != null && birthEndDate != null) {
            if (birthStartDate.isAfter(birthEndDate)) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
        }
    }

    /**
     * 创建基础分页查询
     */
    public static UserQueryRequest pageQuery(Integer pageNum, Integer pageSize) {
        return new UserQueryRequest(pageNum, pageSize, null, null, null, null,
                null, null, null, null);
    }

    /**
     * 按手机号查询
     */
    public static UserQueryRequest byMobile(String mobile) {
        return new UserQueryRequest(1, 1, mobile, null, null, null,
                null, null, null, null);
    }

    /**
     * 按状态查询
     */
    public static UserQueryRequest byStatus(Integer status, Integer pageNum, Integer pageSize) {
        return new UserQueryRequest(pageNum, pageSize, null, null, null, null,
                status, null, null, null);
    }

    /**
     * 按客户端类型查询
     */
    public static UserQueryRequest byClientType(String clientType, Integer pageNum, Integer pageSize) {
        return new UserQueryRequest(pageNum, pageSize, null, null, null, null,
                null, null, null, clientType);
    }
}
