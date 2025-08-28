package com.xypai.user.domain.shared;

import com.xypai.user.domain.entity.Transaction;
import com.xypai.user.domain.valueobject.Money;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.domain.valueobject.WalletId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 💳 钱包充值事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record WalletRechargedEvent(
        String eventId,
        WalletId walletId,
        UserId userId,
        Money amount,
        Transaction.TransactionId transactionId,
        Instant occurredOn
) implements DomainEvent {

    public WalletRechargedEvent {
        Objects.requireNonNull(eventId, "事件ID不能为空");
        Objects.requireNonNull(walletId, "钱包ID不能为空");
        Objects.requireNonNull(userId, "用户ID不能为空");
        Objects.requireNonNull(amount, "充值金额不能为空");
        Objects.requireNonNull(transactionId, "交易ID不能为空");
        Objects.requireNonNull(occurredOn, "发生时间不能为空");
    }

    /**
     * 静态工厂方法：创建钱包充值事件
     */
    public static WalletRechargedEvent create(
            WalletId walletId,
            UserId userId,
            Money amount,
            Transaction.TransactionId transactionId
    ) {
        return new WalletRechargedEvent(
                UUID.randomUUID().toString(),
                walletId,
                userId,
                amount,
                transactionId,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "wallet.recharged";
    }
}
