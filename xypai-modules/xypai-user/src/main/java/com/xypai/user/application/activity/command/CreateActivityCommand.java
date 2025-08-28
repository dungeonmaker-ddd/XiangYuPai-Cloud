package com.xypai.user.application.activity.command;

import com.xypai.user.domain.activity.entity.ActivityInfo;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 🔨 创建活动命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record CreateActivityCommand(
        @NotNull(message = "组织者ID不能为空")
        UserId organizerId,

        @NotNull(message = "活动信息不能为空")
        ActivityInfo activityInfo
) {

    public CreateActivityCommand {
        // 验证组织者ID
        if (organizerId == null) {
            throw new IllegalArgumentException("组织者ID不能为空");
        }

        // 验证活动信息
        if (activityInfo == null) {
            throw new IllegalArgumentException("活动信息不能为空");
        }
    }
}
