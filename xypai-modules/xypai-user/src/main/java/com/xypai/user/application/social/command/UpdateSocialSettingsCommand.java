package com.xypai.user.application.social.command;

import com.xypai.user.domain.social.entity.SocialSettings;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * ⚙️ 更新社交设置命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UpdateSocialSettingsCommand(
        @NotNull(message = "用户ID不能为空")
        UserId userId,

        @NotNull(message = "社交设置不能为空")
        SocialSettings settings
) {
}
