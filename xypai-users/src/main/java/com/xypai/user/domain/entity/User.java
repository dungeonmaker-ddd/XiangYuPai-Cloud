package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.xypai.common.core.web.domain.BaseEntity;
import com.xypai.common.sensitive.annotation.Sensitive;
import com.xypai.common.sensitive.enums.DesensitizedType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 🏗️ XY相遇派用户实体 - 企业架构实现
 * <p>
 * 遵循企业微服务架构规范：
 * - 继承BaseEntity获得审计字段
 * - 使用@Sensitive进行敏感数据脱敏
 * - 支持逻辑删除和多租户
 * - 完整的字段验证注解
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("xypai_users")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID - 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    /**
     * 用户编码 - 业务唯一标识
     */
    @TableField("user_code")
    @NotBlank(message = "用户编码不能为空")
    @Size(max = 32, message = "用户编码长度不能超过32字符")
    private String userCode;

    /**
     * 手机号 - 敏感数据脱敏
     */
    @TableField("mobile")
    @Sensitive(desensitizedType = DesensitizedType.PHONE)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 用户名
     */
    @TableField("username")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 100, message = "用户昵称长度不能超过100字符")
    private String nickname;

    /**
     * 邮箱 - 敏感数据脱敏
     */
    @TableField("email")
    @Sensitive(desensitizedType = DesensitizedType.EMAIL)
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100字符")
    private String email;

    /**
     * 真实姓名 - 敏感数据脱敏
     */
    @TableField("real_name")
    @Sensitive(desensitizedType = DesensitizedType.USERNAME)
    @Size(max = 50, message = "真实姓名长度不能超过50字符")
    private String realName;

    /**
     * 身份证号 - 敏感数据脱敏
     */
    @TableField("id_card")
    @Sensitive(desensitizedType = DesensitizedType.ID_CARD)
    @Pattern(regexp = "^\\d{17}[0-9Xx]$", message = "身份证号格式不正确")
    private String idCard;

    /**
     * 性别: 0-未知, 1-男, 2-女, 3-其他
     */
    @TableField("gender")
    @Min(value = 0, message = "性别值不能小于0")
    @Max(value = 3, message = "性别值不能大于3")
    private Integer gender;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    @Size(max = 500, message = "头像URL长度不能超过500字符")
    private String avatarUrl;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDateTime birthday;

    /**
     * 所在地区
     */
    @TableField("location")
    @Size(max = 200, message = "所在地区长度不能超过200字符")
    private String location;

    /**
     * 个人简介
     */
    @TableField("bio")
    @Size(max = 500, message = "个人简介长度不能超过500字符")
    private String bio;

    /**
     * 用户状态: 0-禁用, 1-正常, 2-冻结, 3-注销
     */
    @TableField("status")
    @NotNull(message = "用户状态不能为空")
    @Min(value = 0, message = "用户状态值不能小于0")
    @Max(value = 3, message = "用户状态值不能大于3")
    private Integer status;

    /**
     * 用户类型: 0-普通用户, 1-VIP用户, 2-SVIP用户, 3-企业用户
     */
    @TableField("user_type")
    @NotNull(message = "用户类型不能为空")
    @Min(value = 0, message = "用户类型值不能小于0")
    @Max(value = 3, message = "用户类型值不能大于3")
    private Integer userType;

    /**
     * 是否实名认证: 0-未认证, 1-已认证
     */
    @TableField("is_verified")
    @NotNull(message = "实名认证状态不能为空")
    @Min(value = 0, message = "实名认证状态值不能小于0")
    @Max(value = 1, message = "实名认证状态值不能大于1")
    private Integer isVerified;

    /**
     * 注册平台: iOS, Android, Web, WeChat
     */
    @TableField("platform")
    @Size(max = 50, message = "注册平台长度不能超过50字符")
    private String platform;

    /**
     * 注册来源渠道
     */
    @TableField("source_channel")
    @Size(max = 100, message = "注册来源渠道长度不能超过100字符")
    private String sourceChannel;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    @Size(max = 50, message = "最后登录IP长度不能超过50字符")
    private String lastLoginIp;

    /**
     * 登录次数
     */
    @TableField("login_count")
    @Min(value = 0, message = "登录次数不能小于0")
    private Integer loginCount;

    /**
     * 用户等级
     */
    @TableField("user_level")
    @Min(value = 1, message = "用户等级不能小于1")
    @Max(value = 100, message = "用户等级不能大于100")
    private Integer userLevel;

    /**
     * 用户积分
     */
    @TableField("user_points")
    @Min(value = 0, message = "用户积分不能小于0")
    private Integer userPoints;

    /**
     * 用户余额(分)
     */
    @TableField("balance")
    @Min(value = 0, message = "用户余额不能小于0")
    private Long balance;

    /**
     * 部门ID - 数据权限关联
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 租户ID - 多租户支持
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 逻辑删除标志: 0-正常, 1-删除
     */
    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /**
     * 版本号 - 乐观锁
     */
    @Version
    @TableField("version")
    private Integer version;
}
