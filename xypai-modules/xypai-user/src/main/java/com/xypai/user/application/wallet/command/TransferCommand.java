package com.xypai.user.application.wallet.command;

import com.xypai.user.domain.shared.Money;
import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 💸 转账命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record TransferCommand(
        @NotNull(message = "转出用户ID不能为空")
        UserId fromUserId,

        @NotNull(message = "转入用户ID不能为空")
        UserId toUserId,

        @NotNull(message = "转账金额不能为空")
        Money amount,

        @Size(max = 200, message = "备注长度不能超过200字符")
        String memo,

        @NotNull(message = "支付密码不能为空")
        String paymentPassword
) {

    public TransferCommand {
        if (fromUserId == null) {
            throw new IllegalArgumentException("转出用户ID不能为空");
        }
        if (toUserId == null) {
            throw new IllegalArgumentException("转入用户ID不能为空");
        }
        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("不能向自己转账");
        }
        if (amount == null) {
            throw new IllegalArgumentException("转账金额不能为空");
        }
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("转账金额必须大于0");
        }
        if (paymentPassword == null || paymentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("支付密码不能为空");
        }
    }
}
