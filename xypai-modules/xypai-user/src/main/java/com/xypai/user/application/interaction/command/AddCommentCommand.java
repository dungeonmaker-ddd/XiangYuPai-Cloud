package com.xypai.user.application.interaction.command;

import com.xypai.user.domain.interaction.enums.TargetType;
import com.xypai.user.domain.interaction.valueobject.TargetId;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 添加评论命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record AddCommentCommand(
        @NotNull(message = "用户ID不能为空")
        UserId userId,

        @NotNull(message = "目标ID不能为空")
        TargetId targetId,

        @NotNull(message = "目标类型不能为空")
        TargetType targetType,

        @NotBlank(message = "评论内容不能为空")
        @Size(min = 1, max = 500, message = "评论内容长度必须在1-500字符之间")
        String content
) {
}
