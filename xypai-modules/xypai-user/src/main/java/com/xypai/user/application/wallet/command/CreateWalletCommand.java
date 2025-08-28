package com.xypai.user.application.wallet.command;

import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

/**
 * 🔨 创建钱包命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record CreateWalletCommand(
        @NotNull(message = "用户ID不能为空")
        UserId userId
) {

    public CreateWalletCommand {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
    }
}
