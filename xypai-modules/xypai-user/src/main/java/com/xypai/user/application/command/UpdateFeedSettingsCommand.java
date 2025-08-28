package com.xypai.user.application.command;

import com.xypai.user.domain.entity.FeedSettings;
import com.xypai.user.domain.valueobject.FeedId;
import com.xypai.user.domain.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 更新动态设置命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UpdateFeedSettingsCommand(
        @NotNull(message = "动态ID不能为空")
        FeedId feedId,

        @NotNull(message = "作者ID不能为空")
        UserId authorId,

        @NotNull(message = "动态设置不能为空")
        FeedSettings settings
) {
}
