package com.xypai.user.domain.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * APP用户实体类 - 2025年现代化版本
 * 使用Lombok大幅简化代码，减少样板代码
 *
 * @author XyPai
 */
@Data                           // 自动生成getter/setter/toString/equals/hashCode
@NoArgsConstructor             // 生成无参构造器
@AllArgsConstructor            // 生成全参构造器
@Builder                       // 支持Builder模式
@Accessors(chain = true)       // 支持链式调用
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // 只包含指定字段
@ToString(exclude = {"deleted", "deleteTime"})    // 排除敏感字段
@TableName("app_user")
public class AppUser implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // 基础信息
    // ========================================

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    @EqualsAndHashCode.Include  // 只用ID做equals/hashCode
    private Long userId;

    /**
     * 手机号（唯一）
     */
    @TableField("mobile")
    @NonNull  // Lombok null检查
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
    @Builder.Default  // Builder模式默认值
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
    // 业务方法（现代化）
    // ========================================

    /**
     * 创建新用户
     */
    public static AppUser createUser(String mobile, String nickname) {
        return AppUser.builder()
                .mobile(mobile)
                .nickname(nickname)
                .build();
    }

    /**
     * 从注册请求创建用户
     */
    public static AppUser fromRegisterRequest(String mobile, String nickname,
                                              Integer gender, String clientType) {
        return AppUser.builder()
                .mobile(mobile)
                .nickname(nickname)
                .gender(gender != null ? gender : 0)
                .clientType(clientType != null ? clientType : "app")
                .build();
    }

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
    public AppUser softDelete() {
        this.deleted = 1;
        this.deleteTime = LocalDateTime.now();
        return this;
    }

    // ========================================
    // 静态工厂方法
    // ========================================

    /**
     * 恢复用户
     */
    public AppUser restore() {
        this.deleted = 0;
        this.deleteTime = null;
        return this;
    }

    /**
     * 更新最后登录时间
     */
    public AppUser updateLastLogin() {
        this.lastLoginTime = LocalDateTime.now();
        return this;
    }
}
