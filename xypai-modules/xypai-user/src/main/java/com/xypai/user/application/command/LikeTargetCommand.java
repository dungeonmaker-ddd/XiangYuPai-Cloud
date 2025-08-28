package com.xypai.user.application.command;

import com.xypai.user.domain.enums.TargetType;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 点赞目标命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record LikeTargetCommand(
        @NotNull(message = "用户ID不能为空")
        UserId userId,

        @NotNull(message = "目标ID不能为空")
        TargetId targetId,

        @NotNull(message = "目标类型不能为空")
        TargetType targetType
) {
}
