package com.xypai.user.domain.entity;

import com.xypai.user.domain.enums.TransactionStatus;
import com.xypai.user.domain.enums.TransactionType;
import com.xypai.user.domain.valueobject.Money;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.domain.valueobject.WalletId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 交易实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record Transaction(
        TransactionId transactionId,
        WalletId walletId,
        TransactionType type,
        Money amount,
        Money fee,
        UserId fromUserId,
        UserId toUserId,
        String description,
        String memo,
        TransactionStatus status,
        String externalTransactionId,
        LocalDateTime createTime,
        LocalDateTime completeTime,
        String failureReason
) {

    public Transaction {
        Objects.requireNonNull(transactionId, "交易ID不能为空");
        Objects.requireNonNull(walletId, "钱包ID不能为空");
        Objects.requireNonNull(type, "交易类型不能为空");
        Objects.requireNonNull(amount, "交易金额不能为空");
        Objects.requireNonNull(status, "交易状态不能为空");
        Objects.requireNonNull(createTime, "创建时间不能为空");

        // 业务规则验证
        if (amount.isZero()) {
            throw new IllegalArgumentException("交易金额不能为零");
        }

        if (fee != null && fee.isGreaterThan(amount)) {
            throw new IllegalArgumentException("手续费不能大于交易金额");
        }

        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("交易描述不能超过200个字符");
        }

        if (memo != null && memo.length() > 500) {
            throw new IllegalArgumentException("交易备注不能超过500个字符");
        }
    }

    /**
     * 静态工厂方法：创建充值交易
     */
    public static Transaction createRecharge(
            WalletId walletId,
            UserId userId,
            Money amount,
            String description
    ) {
        return new Transaction(
                TransactionId.generate(),
                walletId,
                TransactionType.RECHARGE,
                amount,
                Money.zeroCny(), // 充值无手续费
                null, // 充值无发送方
                userId,
                description,
                null,
                TransactionStatus.PENDING,
                null,
                LocalDateTime.now(),
                null,
                null
        );
    }

    /**
     * 静态工厂方法：创建提现交易
     */
    public static Transaction createWithdraw(
            WalletId walletId,
            UserId userId,
            Money amount,
            Money fee,
            String description
    ) {
        return new Transaction(
                TransactionId.generate(),
                walletId,
                TransactionType.WITHDRAW,
                amount,
                fee,
                userId,
                null, // 提现无接收方
                description,
                null,
                TransactionStatus.PENDING,
                null,
                LocalDateTime.now(),
                null,
                null
        );
    }

    /**
     * 静态工厂方法：创建转账交易
     */
    public static Transaction createTransfer(
            WalletId fromWalletId,
            UserId fromUserId,
            UserId toUserId,
            Money amount,
            Money fee,
            String memo
    ) {
        return new Transaction(
                TransactionId.generate(),
                fromWalletId,
                TransactionType.TRANSFER_OUT,
                amount,
                fee,
                fromUserId,
                toUserId,
                "转账给用户" + toUserId.value(),
                memo,
                TransactionStatus.PENDING,
                null,
                LocalDateTime.now(),
                null,
                null
        );
    }

    /**
     * 静态工厂方法：创建支付交易
     */
    public static Transaction createPayment(
            WalletId walletId,
            UserId userId,
            Money amount,
            String description,
            String externalTransactionId
    ) {
        return new Transaction(
                TransactionId.generate(),
                walletId,
                TransactionType.PAYMENT,
                amount,
                Money.zeroCny(),
                userId,
                null,
                description,
                null,
                TransactionStatus.PENDING,
                externalTransactionId,
                LocalDateTime.now(),
                null,
                null
        );
    }

    /**
     * 完成交易
     */
    public Transaction complete() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("只有待处理状态的交易才能完成");
        }

        return new Transaction(
                this.transactionId,
                this.walletId,
                this.type,
                this.amount,
                this.fee,
                this.fromUserId,
                this.toUserId,
                this.description,
                this.memo,
                TransactionStatus.SUCCESS,
                this.externalTransactionId,
                this.createTime,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * 交易失败
     */
    public Transaction fail(String reason) {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("只有待处理状态的交易才能标记为失败");
        }

        return new Transaction(
                this.transactionId,
                this.walletId,
                this.type,
                this.amount,
                this.fee,
                this.fromUserId,
                this.toUserId,
                this.description,
                this.memo,
                TransactionStatus.FAILED,
                this.externalTransactionId,
                this.createTime,
                LocalDateTime.now(),
                reason
        );
    }

    /**
     * 取消交易
     */
    public Transaction cancel() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("只有待处理状态的交易才能取消");
        }

        return new Transaction(
                this.transactionId,
                this.walletId,
                this.type,
                this.amount,
                this.fee,
                this.fromUserId,
                this.toUserId,
                this.description,
                this.memo,
                TransactionStatus.CANCELLED,
                this.externalTransactionId,
                this.createTime,
                LocalDateTime.now(),
                "用户取消"
        );
    }

    /**
     * 检查交易是否完成
     */
    public boolean isCompleted() {
        return status == TransactionStatus.SUCCESS;
    }

    /**
     * 检查交易是否失败
     */
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

    /**
     * 检查交易是否可以取消
     */
    public boolean canCancel() {
        return status == TransactionStatus.PENDING;
    }

    /**
     * 获取实际扣除金额（包含手续费）
     */
    public Money getTotalDeduction() {
        return fee != null ? amount.add(fee) : amount;
    }

    /**
     * 交易ID值对象
     */
    public record TransactionId(String value) {
        public TransactionId {
            Objects.requireNonNull(value, "交易ID不能为空");
        }

        public static TransactionId generate() {
            return new TransactionId("txn_" + UUID.randomUUID().toString().replace("-", ""));
        }

        public static TransactionId of(String value) {
            return new TransactionId(value);
        }
    }
}
