package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 👤 用户扩展信息实体 - MVP版本
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Data
@Accessors(chain = true)
@TableName("user_profile")
public class UserProfile {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 生日
     */
    private LocalDate birthDate;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 兴趣爱好
     */
    private String interests;

    /**
     * 隐私级别 (1-5)
     */
    private Integer privacyLevel;

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
}
