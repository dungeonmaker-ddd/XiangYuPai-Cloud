package com.xypai.user.application.interaction.command;

import com.xypai.user.domain.interaction.enums.TargetType;
import com.xypai.user.domain.interaction.valueobject.TargetId;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 取消点赞目标命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UnlikeTargetCommand(
        @NotNull(message = "用户ID不能为空")
        UserId userId,

        @NotNull(message = "目标ID不能为空")
        TargetId targetId,

        @NotNull(message = "目标类型不能为空")
        TargetType targetType
) {
}
