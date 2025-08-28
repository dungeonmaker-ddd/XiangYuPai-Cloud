package com.xypai.user.application.social.command;

import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 👥 取消关注用户命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UnfollowUserCommand(
        @NotNull(message = "关注者ID不能为空")
        UserId followerId,

        @NotNull(message = "被关注者ID不能为空")
        UserId followeeId
) {

    public UnfollowUserCommand {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("不能对自己执行此操作");
        }
    }
}
