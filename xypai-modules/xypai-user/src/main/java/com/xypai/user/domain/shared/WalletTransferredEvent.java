package com.xypai.user.domain.shared;

import com.xypai.user.domain.entity.Transaction;
import com.xypai.user.domain.valueobject.Money;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.domain.valueobject.WalletId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 💰 钱包转账事件
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record WalletTransferredEvent(
        String eventId,
        WalletId walletId,
        UserId fromUserId,
        UserId toUserId,
        Money amount,
        Transaction.TransactionId transactionId,
        Instant occurredOn
) implements DomainEvent {

    public WalletTransferredEvent {
        Objects.requireNonNull(eventId, "事件ID不能为空");
        Objects.requireNonNull(walletId, "钱包ID不能为空");
        Objects.requireNonNull(fromUserId, "发送用户ID不能为空");
        Objects.requireNonNull(toUserId, "接收用户ID不能为空");
        Objects.requireNonNull(amount, "转账金额不能为空");
        Objects.requireNonNull(transactionId, "交易ID不能为空");
        Objects.requireNonNull(occurredOn, "发生时间不能为空");
    }

    /**
     * 静态工厂方法：创建钱包转账事件
     */
    public static WalletTransferredEvent create(
            WalletId walletId,
            UserId fromUserId,
            UserId toUserId,
            Money amount,
            Transaction.TransactionId transactionId
    ) {
        return new WalletTransferredEvent(
                UUID.randomUUID().toString(),
                walletId,
                fromUserId,
                toUserId,
                amount,
                transactionId,
                Instant.now()
        );
    }

    @Override
    public String eventType() {
        return "wallet.transferred";
    }
}
