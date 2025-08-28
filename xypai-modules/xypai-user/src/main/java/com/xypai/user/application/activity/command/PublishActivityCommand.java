package com.xypai.user.application.activity.command;

import com.xypai.user.domain.activity.valueobject.ActivityId;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 📢 发布活动命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record PublishActivityCommand(
        @NotNull(message = "活动ID不能为空")
        ActivityId activityId,

        @NotNull(message = "组织者ID不能为空")
        UserId organizerId
) {

    public PublishActivityCommand {
        if (activityId == null) {
            throw new IllegalArgumentException("活动ID不能为空");
        }
        if (organizerId == null) {
            throw new IllegalArgumentException("组织者ID不能为空");
        }
    }
}
