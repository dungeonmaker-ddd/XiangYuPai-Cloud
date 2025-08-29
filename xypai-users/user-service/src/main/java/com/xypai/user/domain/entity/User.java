package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 👤 用户实体 - MVP版本
 * <p>
 * 设计原则：
 * - 简单够用
 * - 快速验证
 * - 易于扩展
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Data
@Accessors(chain = true)
@TableName("user")
public class User {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号 - 唯一
     */
    private String mobile;

    /**
     * 用户名 - 唯一
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别 (0-未知, 1-男, 2-女)
     */
    private Integer gender;

    /**
     * 状态 (1-正常, 2-禁用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记 (0-正常, 1-删除)
     */
    @TableLogic
    private Integer deleted;

    // ========================================
    // 业务方法 - MVP版本
    // ========================================

    /**
     * 🔍 是否正常状态
     */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /**
     * 🔍 获取性别描述
     */
    public String getGenderDesc() {
        if (gender == null) return "未知";
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    /**
     * 🔍 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "正常";
            case 2 -> "禁用";
            default -> "未知";
        };
    }
}
