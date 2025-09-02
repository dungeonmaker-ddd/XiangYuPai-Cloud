package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户基础信息实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识(雪花ID)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 登录用户名(唯一)
     */
    @TableField("username")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含字母、数字、下划线和中文")
    private String username;

    /**
     * 手机号(唯一,登录凭证)
     */
    @TableField("mobile")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 密码哈希值
     */
    @TableField("password")
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 用户状态(0=禁用,1=正常,2=冻结)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 注册时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 是否删除标志(逻辑删除)
     */
    @TableLogic
    @TableField("deleted")
    @Builder.Default
    private Boolean deleted = false;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    /**
     * 用户状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        NORMAL(1, "正常"), 
        FROZEN(2, "冻结");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 检查用户状态是否正常
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }

    /**
     * 检查用户是否被禁用
     */
    public boolean isDisabled() {
        return Status.DISABLED.getCode().equals(this.status);
    }

    /**
     * 检查用户是否被冻结
     */
    public boolean isFrozen() {
        return Status.FROZEN.getCode().equals(this.status);
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        for (Status status : Status.values()) {
            if (status.getCode().equals(this.status)) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }
}
