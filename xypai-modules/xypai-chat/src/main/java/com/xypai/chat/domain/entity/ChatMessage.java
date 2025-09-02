package com.xypai.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天消息实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "chat_message", autoResultMap = true)
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属会话ID
     */
    @TableField("conversation_id")
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 发送者ID(NULL=系统消息)
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 消息类型(1=文本,2=图片,3=语音,4=视频,5=文件,6=系统通知)
     */
    @TableField("message_type")
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /**
     * 消息内容(文本/文件名/系统通知文本)
     */
    @TableField("content")
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 媒体数据JSON{url,size,duration...}
     */
    @TableField(value = "media_data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> mediaData;

    /**
     * 回复的消息ID(引用回复)
     */
    @TableField("reply_to_id")
    private Long replyToId;

    /**
     * 消息状态(0=已删除,1=正常,2=已撤回)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 发送时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        TEXT(1, "文本"),
        IMAGE(2, "图片"),
        VOICE(3, "语音"),
        VIDEO(4, "视频"),
        FILE(5, "文件"),
        SYSTEM(6, "系统通知"),
        EMOJI(7, "表情"),
        LOCATION(8, "位置");

        private final Integer code;
        private final String desc;

        MessageType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static MessageType fromCode(Integer code) {
            for (MessageType type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 消息状态枚举
     */
    public enum Status {
        DELETED(0, "已删除"),
        NORMAL(1, "正常"),
        RECALLED(2, "已撤回");

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
     * 是否为文本消息
     */
    public boolean isText() {
        return MessageType.TEXT.getCode().equals(this.messageType);
    }

    /**
     * 是否为图片消息
     */
    public boolean isImage() {
        return MessageType.IMAGE.getCode().equals(this.messageType);
    }

    /**
     * 是否为语音消息
     */
    public boolean isVoice() {
        return MessageType.VOICE.getCode().equals(this.messageType);
    }

    /**
     * 是否为视频消息
     */
    public boolean isVideo() {
        return MessageType.VIDEO.getCode().equals(this.messageType);
    }

    /**
     * 是否为文件消息
     */
    public boolean isFile() {
        return MessageType.FILE.getCode().equals(this.messageType);
    }

    /**
     * 是否为系统消息
     */
    public boolean isSystem() {
        return MessageType.SYSTEM.getCode().equals(this.messageType) || senderId == null;
    }

    /**
     * 是否正常状态
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }

    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return Status.DELETED.getCode().equals(this.status);
    }

    /**
     * 是否已撤回
     */
    public boolean isRecalled() {
        return Status.RECALLED.getCode().equals(this.status);
    }

    /**
     * 是否为回复消息
     */
    public boolean isReply() {
        return replyToId != null;
    }

    /**
     * 获取消息类型描述
     */
    public String getMessageTypeDesc() {
        MessageType type = MessageType.fromCode(this.messageType);
        return type != null ? type.getDesc() : "未知";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status messageStatus = Status.fromCode(this.status);
        return messageStatus != null ? messageStatus.getDesc() : "未知";
    }

    /**
     * 获取媒体URL
     */
    public String getMediaUrl() {
        return mediaData != null ? (String) mediaData.get("url") : null;
    }

    /**
     * 设置媒体URL
     */
    public void setMediaUrl(String url) {
        if (mediaData == null) {
            mediaData = new java.util.HashMap<>();
        }
        mediaData.put("url", url);
    }

    /**
     * 获取文件大小
     */
    public Long getFileSize() {
        return mediaData != null ? ((Number) mediaData.get("size")).longValue() : null;
    }

    /**
     * 设置文件大小
     */
    public void setFileSize(Long size) {
        if (mediaData == null) {
            mediaData = new java.util.HashMap<>();
        }
        mediaData.put("size", size);
    }

    /**
     * 获取媒体时长(秒)
     */
    public Integer getDuration() {
        return mediaData != null ? (Integer) mediaData.get("duration") : null;
    }

    /**
     * 设置媒体时长
     */
    public void setDuration(Integer duration) {
        if (mediaData == null) {
            mediaData = new java.util.HashMap<>();
        }
        mediaData.put("duration", duration);
    }

    /**
     * 获取缩略图URL
     */
    public String getThumbnailUrl() {
        return mediaData != null ? (String) mediaData.get("thumbnail") : null;
    }

    /**
     * 设置缩略图URL
     */
    public void setThumbnailUrl(String thumbnail) {
        if (mediaData == null) {
            mediaData = new java.util.HashMap<>();
        }
        mediaData.put("thumbnail", thumbnail);
    }

    /**
     * 检查是否可以撤回
     */
    public boolean canRecall(Long currentUserId) {
        // 只有发送者可以撤回，且在5分钟内
        return senderId != null && senderId.equals(currentUserId) && 
               createdAt != null && createdAt.isAfter(LocalDateTime.now().minusMinutes(5)) &&
               isNormal();
    }
}
