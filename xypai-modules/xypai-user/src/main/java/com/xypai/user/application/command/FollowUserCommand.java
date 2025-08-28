package com.xypai.user.application.command;

import com.xypai.user.domain.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 👥 关注用户命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record FollowUserCommand(
        @NotNull(message = "关注者ID不能为空")
        UserId followerId,

        @NotNull(message = "被关注者ID不能为空")
        UserId followeeId
) {

    public FollowUserCommand {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("不能关注自己");
        }
    }
}
