package com.xypai.user.application.activity.command;

import com.xypai.user.domain.activity.valueobject.ActivityId;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 👥 参与活动命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record JoinActivityCommand(
        @NotNull(message = "活动ID不能为空")
        ActivityId activityId,

        @NotNull(message = "参与者ID不能为空")
        UserId participantId
) {

    public JoinActivityCommand {
        if (activityId == null) {
            throw new IllegalArgumentException("活动ID不能为空");
        }
        if (participantId == null) {
            throw new IllegalArgumentException("参与者ID不能为空");
        }
    }
}
