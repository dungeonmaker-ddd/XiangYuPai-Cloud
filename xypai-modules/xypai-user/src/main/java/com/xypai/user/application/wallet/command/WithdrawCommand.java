package com.xypai.user.application.wallet.command;

import com.xypai.user.domain.shared.Money;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 🏧 提现命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record WithdrawCommand(
        @NotNull(message = "用户ID不能为空")
        UserId userId,

        @NotNull(message = "提现金额不能为空")
        Money amount,

        @NotNull(message = "银行账户不能为空")
        String bankAccount,

        @NotNull(message = "支付密码不能为空")
        String paymentPassword
) {

    public WithdrawCommand {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (amount == null) {
            throw new IllegalArgumentException("提现金额不能为空");
        }
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("提现金额必须大于0");
        }
        if (bankAccount == null || bankAccount.trim().isEmpty()) {
            throw new IllegalArgumentException("银行账户不能为空");
        }
        if (paymentPassword == null || paymentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("支付密码不能为空");
        }
    }
}
