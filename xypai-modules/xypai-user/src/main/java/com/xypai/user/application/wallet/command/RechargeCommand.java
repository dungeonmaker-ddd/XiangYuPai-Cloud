package com.xypai.user.application.wallet.command;

import com.xypai.user.domain.shared.Money;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 💳 充值命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record RechargeCommand(
        @NotNull(message = "用户ID不能为空")
        UserId userId,

        @NotNull(message = "充值金额不能为空")
        Money amount,

        @NotNull(message = "支付方式不能为空")
        String paymentMethod
) {

    public RechargeCommand {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (amount == null) {
            throw new IllegalArgumentException("充值金额不能为空");
        }
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("支付方式不能为空");
        }
    }
}
