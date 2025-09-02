package com.xypai.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 万能内容实体(动态/活动/技能)
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "content", autoResultMap = true)
public class Content implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容唯一ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建者ID
     */
    @TableField("user_id")
    @NotNull(message = "创建者ID不能为空")
    private Long userId;

    /**
     * 内容类型(1=动态,2=活动,3=技能)
     */
    @TableField("type")
    @NotNull(message = "内容类型不能为空")
    private Integer type;

    /**
     * 内容标题
     */
    @TableField("title")
    @NotBlank(message = "内容标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    /**
     * 类型特定数据JSON
     */
    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    @NotNull(message = "内容数据不能为空")
    private Map<String, Object> data;

    /**
     * 内容状态(0=草稿,1=发布,2=下架)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 0;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

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
     * 内容类型枚举
     */
    public enum Type {
        FEED(1, "动态"),
        ACTIVITY(2, "活动"),
        SKILL(3, "技能");

        private final Integer code;
        private final String desc;

        Type(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Type fromCode(Integer code) {
            for (Type type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 内容状态枚举
     */
    public enum Status {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "发布"),
        ARCHIVED(2, "下架");

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

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 是否为动态
     */
    public boolean isFeed() {
        return Type.FEED.getCode().equals(this.type);
    }

    /**
     * 是否为活动
     */
    public boolean isActivity() {
        return Type.ACTIVITY.getCode().equals(this.type);
    }

    /**
     * 是否为技能
     */
    public boolean isSkill() {
        return Type.SKILL.getCode().equals(this.type);
    }

    /**
     * 是否为草稿状态
     */
    public boolean isDraft() {
        return Status.DRAFT.getCode().equals(this.status);
    }

    /**
     * 是否已发布
     */
    public boolean isPublished() {
        return Status.PUBLISHED.getCode().equals(this.status);
    }

    /**
     * 是否已下架
     */
    public boolean isArchived() {
        return Status.ARCHIVED.getCode().equals(this.status);
    }

    /**
     * 获取内容类型描述
     */
    public String getTypeDesc() {
        Type contentType = Type.fromCode(this.type);
        return contentType != null ? contentType.getDesc() : "未知";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status contentStatus = Status.fromCode(this.status);
        return contentStatus != null ? contentStatus.getDesc() : "未知";
    }

    /**
     * 获取内容摘要
     */
    public String getSummary() {
        if (data != null && data.containsKey("summary")) {
            return (String) data.get("summary");
        }
        return title.length() > 50 ? title.substring(0, 47) + "..." : title;
    }

    /**
     * 获取封面图片
     */
    public String getCoverImage() {
        return data != null ? (String) data.get("cover_image") : null;
    }

    /**
     * 设置封面图片
     */
    public void setCoverImage(String coverImage) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put("cover_image", coverImage);
    }
}
