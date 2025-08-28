package com.xypai.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户持久化对象 - 基础设施层
 * <p>
 * 职责：数据库映射和持久化
 *
 * @author XyPai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"deleted", "deleteTime"})
@TableName("app_user")
public class UserPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    @EqualsAndHashCode.Include
    private Long userId;

    /**
     * 手机号（唯一）
     */
    @TableField("mobile")
    @NonNull
    private String mobile;

    /**
     * 用户名（可选）
     */
    @TableField("username")
    private String username;

    /**
     * 昵称
     */
    @TableField("nickname")
    @NonNull
    private String nickname;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别 0-未知 1-男 2-女
     */
    @TableField("gender")
    @Builder.Default
    private Integer gender = 0;

    /**
     * 生日
     */
    @TableField("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * 状态 1-正常 0-禁用
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 注册时间
     */
    @TableField(value = "register_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 客户端类型：web、app、mini
     */
    @TableField("client_type")
    @Builder.Default
    private String clientType = "app";

    // ========================================
    // 详细资料字段
    // ========================================

    @TableField("real_name")
    private String realName;

    @TableField("email")
    private String email;

    @TableField("wechat")
    private String wechat;

    @TableField("occupation")
    private String occupation;

    @TableField("location")
    private String location;

    @TableField("bio")
    private String bio;

    @TableField("interests")
    private String interests;

    @TableField("height")
    private BigDecimal height;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("notification_push")
    @Builder.Default
    private Integer notificationPush = 1;

    @TableField("privacy_level")
    @Builder.Default
    private Integer privacyLevel = 1;

    @TableField("language")
    @Builder.Default
    private String language = "zh-CN";

    // ========================================
    // 软删除相关字段
    // ========================================

    @TableField("deleted")
    @Builder.Default
    private Integer deleted = 0;

    @TableField("delete_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;

    // ========================================
    // 审计字段
    // ========================================

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ========================================
    // 持久化辅助方法
    // ========================================

    /**
     * 检查用户是否正常
     */
    public boolean isActive() {
        return status != null && status == 1 && deleted != null && deleted == 0;
    }

    /**
     * 检查用户是否已删除
     */
    public boolean isDeleted() {
        return deleted != null && deleted == 1;
    }

    /**
     * 软删除用户
     */
    public UserPO softDelete() {
        this.deleted = 1;
        this.deleteTime = LocalDateTime.now();
        return this;
    }

    /**
     * 恢复用户
     */
    public UserPO restore() {
        this.deleted = 0;
        this.deleteTime = null;
        return this;
    }

    /**
     * 更新最后登录时间
     */
    public UserPO updateLastLogin() {
        this.lastLoginTime = LocalDateTime.now();
        return this;
    }
}
