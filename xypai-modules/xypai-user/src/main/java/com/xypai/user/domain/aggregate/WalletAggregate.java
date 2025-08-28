package com.xypai.user.domain.aggregate;

import com.xypai.user.domain.entity.Transaction;
import com.xypai.user.domain.entity.WalletSettings;
import com.xypai.user.domain.enums.TransactionType;
import com.xypai.user.domain.enums.WalletStatus;
import com.xypai.user.domain.shared.DomainEvent;
import com.xypai.user.domain.shared.WalletRechargedEvent;
import com.xypai.user.domain.shared.WalletTransferredEvent;
import com.xypai.user.domain.valueobject.Money;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.domain.valueobject.WalletId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 💰 钱包聚合根 - 处理用户财务和交易
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class WalletAggregate {

    private final WalletId walletId;
    private final UserId userId;
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private Money balance;
    private Money frozenBalance;
    private WalletStatus status;
    private WalletSettings settings;
    private List<Transaction> transactions;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String paymentPassword;
    private LocalDateTime lastTransactionTime;

    // ========================================
    // 构造器
    // ========================================

    private WalletAggregate(
            WalletId walletId,
            UserId userId,
            Money balance,
            Money frozenBalance,
            WalletStatus status,
            WalletSettings settings,
            List<Transaction> transactions,
            LocalDateTime createTime,
            LocalDateTime updateTime,
            String paymentPassword,
            LocalDateTime lastTransactionTime
    ) {
        this.walletId = Objects.requireNonNull(walletId, "钱包ID不能为空");
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.balance = Objects.requireNonNull(balance, "余额不能为空");
        this.frozenBalance = Objects.requireNonNull(frozenBalance, "冻结余额不能为空");
        this.status = Objects.requireNonNull(status, "钱包状态不能为空");
        this.settings = Objects.requireNonNull(settings, "钱包设置不能为空");
        this.transactions = new ArrayList<>(Objects.requireNonNull(transactions, "交易记录不能为空"));
        this.createTime = Objects.requireNonNull(createTime, "创建时间不能为空");
        this.updateTime = updateTime;
        this.paymentPassword = paymentPassword;
        this.lastTransactionTime = lastTransactionTime;
    }

    // ========================================
    // 静态工厂方法
    // ========================================

    /**
     * 创建新钱包
     */
    public static WalletAggregate createWallet(UserId userId, String currency) {
        Objects.requireNonNull(userId, "用户ID不能为空");
        Objects.requireNonNull(currency, "货币类型不能为空");

        var walletId = WalletId.fromUserId(userId);
        var createTime = LocalDateTime.now();

        return new WalletAggregate(
                walletId,
                userId,
                Money.zero(currency),
                Money.zero(currency),
                WalletStatus.ACTIVE,
                WalletSettings.defaultSettings(),
                new ArrayList<>(),
                createTime,
                createTime,
                null,
                null
        );
    }

    /**
     * 从持久化数据重构聚合根
     */
    public static WalletAggregate reconstruct(
            WalletId walletId,
            UserId userId,
            Money balance,
            Money frozenBalance,
            WalletStatus status,
            WalletSettings settings,
            List<Transaction> transactions,
            LocalDateTime createTime,
            LocalDateTime updateTime,
            String paymentPassword,
            LocalDateTime lastTransactionTime
    ) {
        return new WalletAggregate(
                walletId,
                userId,
                balance,
                frozenBalance,
                status,
                settings,
                transactions,
                createTime,
                updateTime,
                paymentPassword,
                lastTransactionTime
        );
    }

    // ========================================
    // 业务方法
    // ========================================

    /**
     * 🎯 业务规则：充值
     */
    public DomainEvent recharge(Money amount, String description) {
        validateCanRecharge();
        validateAmount(amount);

        var transaction = Transaction.createRecharge(walletId, userId, amount, description);
        addTransaction(transaction);

        // 增加余额
        this.balance = this.balance.add(amount);
        this.updateTime = LocalDateTime.now();
        this.lastTransactionTime = LocalDateTime.now();

        var event = WalletRechargedEvent.create(walletId, userId, amount, transaction.transactionId());
        addDomainEvent(event);
        return event;
    }

    /**
     * 🎯 业务规则：转账
     */
    public DomainEvent transfer(UserId toUserId, Money amount, String memo) {
        validateCanTransfer();
        validateTransferAmount(amount);
        validateSufficientBalance(amount);
        validateDailyTransferLimit(amount);

        // 计算手续费
        Money fee = calculateTransferFee(amount);
        Money totalAmount = amount.add(fee);

        validateSufficientBalance(totalAmount);

        var transaction = Transaction.createTransfer(walletId, userId, toUserId, amount, fee, memo);
        addTransaction(transaction);

        // 扣除余额
        this.balance = this.balance.subtract(totalAmount);
        this.updateTime = LocalDateTime.now();
        this.lastTransactionTime = LocalDateTime.now();

        var event = WalletTransferredEvent.create(walletId, userId, toUserId, amount, transaction.transactionId());
        addDomainEvent(event);
        return event;
    }

    /**
     * 🎯 业务规则：接收转账
     */
    public DomainEvent receiveTransfer(UserId fromUserId, Money amount, String transactionId) {
        validateCanReceive();
        validateAmount(amount);

        var transaction = new Transaction(
                Transaction.TransactionId.of(transactionId),
                walletId,
                TransactionType.TRANSFER_IN,
                amount,
                Money.zeroCny(),
                fromUserId,
                userId,
                "接收来自用户" + fromUserId.value() + "的转账",
                null,
                com.xypai.user.domain.enums.TransactionStatus.SUCCESS,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        addTransaction(transaction);

        // 增加余额
        this.balance = this.balance.add(amount);
        this.updateTime = LocalDateTime.now();
        this.lastTransactionTime = LocalDateTime.now();

        // 这里不发布事件，因为转账事件由发送方发布
        return null;
    }

    /**
     * 🎯 业务规则：提现
     */
    public DomainEvent withdraw(Money amount, String description) {
        validateCanWithdraw();
        validateWithdrawAmount(amount);
        validateSufficientBalance(amount);
        validateDailyWithdrawLimit(amount);

        // 计算手续费
        Money fee = calculateWithdrawFee(amount);
        Money totalAmount = amount.add(fee);

        validateSufficientBalance(totalAmount);

        var transaction = Transaction.createWithdraw(walletId, userId, amount, fee, description);
        addTransaction(transaction);

        // 先冻结资金
        this.balance = this.balance.subtract(totalAmount);
        this.frozenBalance = this.frozenBalance.add(totalAmount);
        this.updateTime = LocalDateTime.now();
        this.lastTransactionTime = LocalDateTime.now();

        // TODO: 创建提现事件
        return null;
    }

    /**
     * 🎯 业务规则：支付
     */
    public DomainEvent payment(Money amount, String description, String externalTransactionId) {
        validateCanPay();
        validatePaymentAmount(amount);
        validateSufficientBalance(amount);
        validateDailyPaymentLimit(amount);

        var transaction = Transaction.createPayment(walletId, userId, amount, description, externalTransactionId);
        addTransaction(transaction);

        // 扣除余额
        this.balance = this.balance.subtract(amount);
        this.updateTime = LocalDateTime.now();
        this.lastTransactionTime = LocalDateTime.now();

        // TODO: 创建支付事件
        return null;
    }

    /**
     * 🎯 业务规则：冻结钱包
     */
    public void freezeWallet(String reason) {
        if (status == WalletStatus.FROZEN) {
            throw new IllegalStateException("钱包已经被冻结");
        }

        this.status = WalletStatus.FROZEN;
        this.updateTime = LocalDateTime.now();

        // TODO: 创建钱包冻结事件
    }

    /**
     * 🎯 业务规则：解冻钱包
     */
    public void unfreezeWallet() {
        if (status != WalletStatus.FROZEN) {
            throw new IllegalStateException("钱包未被冻结");
        }

        this.status = WalletStatus.ACTIVE;
        this.updateTime = LocalDateTime.now();

        // TODO: 创建钱包解冻事件
    }

    /**
     * 🎯 业务规则：设置支付密码
     */
    public void setPaymentPassword(String newPassword) {
        Objects.requireNonNull(newPassword, "支付密码不能为空");

        if (newPassword.length() != 6) {
            throw new IllegalArgumentException("支付密码必须为6位数字");
        }
        if (!newPassword.matches("\\d{6}")) {
            throw new IllegalArgumentException("支付密码只能包含数字");
        }

        this.paymentPassword = newPassword; // 实际应用中需要加密存储
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 🎯 业务规则：更新钱包设置
     */
    public void updateSettings(WalletSettings newSettings) {
        Objects.requireNonNull(newSettings, "钱包设置不能为空");

        this.settings = newSettings;
        this.updateTime = LocalDateTime.now();
    }

    // ========================================
    // 查询方法
    // ========================================

    /**
     * 获取可用余额
     */
    public Money getAvailableBalance() {
        return balance.subtract(frozenBalance);
    }

    /**
     * 获取今日转账总额
     */
    public Money getTodayTransferAmount() {
        LocalDate today = LocalDate.now();
        return transactions.stream()
                .filter(t -> t.type() == TransactionType.TRANSFER_OUT)
                .filter(t -> t.createTime().toLocalDate().equals(today))
                .filter(t -> t.isCompleted())
                .map(Transaction::amount)
                .reduce(Money.zeroCny(), Money::add);
    }

    /**
     * 获取今日支付总额
     */
    public Money getTodayPaymentAmount() {
        LocalDate today = LocalDate.now();
        return transactions.stream()
                .filter(t -> t.type() == TransactionType.PAYMENT)
                .filter(t -> t.createTime().toLocalDate().equals(today))
                .filter(t -> t.isCompleted())
                .map(Transaction::amount)
                .reduce(Money.zeroCny(), Money::add);
    }

    /**
     * 获取今日提现总额
     */
    public Money getTodayWithdrawAmount() {
        LocalDate today = LocalDate.now();
        return transactions.stream()
                .filter(t -> t.type() == TransactionType.WITHDRAW)
                .filter(t -> t.createTime().toLocalDate().equals(today))
                .filter(t -> t.isCompleted())
                .map(Transaction::amount)
                .reduce(Money.zeroCny(), Money::add);
    }

    /**
     * 检查支付密码
     */
    public boolean verifyPaymentPassword(String password) {
        return paymentPassword != null && paymentPassword.equals(password);
    }

    /**
     * 检查是否有支付密码
     */
    public boolean hasPaymentPassword() {
        return paymentPassword != null && !paymentPassword.isEmpty();
    }

    /**
     * 获取最近的交易记录
     */
    public List<Transaction> getRecentTransactions(int limit) {
        return transactions.stream()
                .sorted((t1, t2) -> t2.createTime().compareTo(t1.createTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ========================================
    // 私有验证方法
    // ========================================

    private void validateCanRecharge() {
        if (!status.canRecharge()) {
            throw new IllegalStateException("当前钱包状态不允许充值: " + status.getDescription());
        }
    }

    private void validateCanTransfer() {
        if (!status.canSend()) {
            throw new IllegalStateException("当前钱包状态不允许转账: " + status.getDescription());
        }
        if (!settings.enableTransfer()) {
            throw new IllegalStateException("转账功能已被禁用");
        }
    }

    private void validateCanReceive() {
        if (!status.canReceive()) {
            throw new IllegalStateException("当前钱包状态不允许接收资金: " + status.getDescription());
        }
    }

    private void validateCanWithdraw() {
        if (!status.canWithdraw()) {
            throw new IllegalStateException("当前钱包状态不允许提现: " + status.getDescription());
        }
        if (!settings.enableWithdraw()) {
            throw new IllegalStateException("提现功能已被禁用");
        }
    }

    private void validateCanPay() {
        if (!status.canSend()) {
            throw new IllegalStateException("当前钱包状态不允许支付: " + status.getDescription());
        }
        if (!settings.enablePayment()) {
            throw new IllegalStateException("支付功能已被禁用");
        }
    }

    private void validateAmount(Money amount) {
        if (amount.isZero()) {
            throw new IllegalArgumentException("金额不能为零");
        }
        if (!amount.currency().equals(balance.currency())) {
            throw new IllegalArgumentException("货币类型不匹配");
        }
    }

    private void validateTransferAmount(Money amount) {
        if (!settings.allowsTransfer(amount)) {
            throw new IllegalArgumentException("转账金额超过单笔限额");
        }
    }

    private void validatePaymentAmount(Money amount) {
        if (!settings.allowsPayment(amount)) {
            throw new IllegalArgumentException("支付金额超过单笔限额");
        }
    }

    private void validateWithdrawAmount(Money amount) {
        if (!settings.allowsWithdraw(amount)) {
            throw new IllegalArgumentException("提现金额超过单笔限额");
        }
    }

    private void validateSufficientBalance(Money amount) {
        if (getAvailableBalance().isLessThan(amount)) {
            throw new IllegalArgumentException("余额不足");
        }
    }

    private void validateDailyTransferLimit(Money amount) {
        Money todayAmount = getTodayTransferAmount();
        Money afterAmount = todayAmount.add(amount);
        if (settings.dailyTransferLimit() != null && afterAmount.isGreaterThan(settings.dailyTransferLimit())) {
            throw new IllegalArgumentException("超过每日转账限额");
        }
    }

    private void validateDailyPaymentLimit(Money amount) {
        Money todayAmount = getTodayPaymentAmount();
        Money afterAmount = todayAmount.add(amount);
        if (settings.dailyPaymentLimit() != null && afterAmount.isGreaterThan(settings.dailyPaymentLimit())) {
            throw new IllegalArgumentException("超过每日支付限额");
        }
    }

    private void validateDailyWithdrawLimit(Money amount) {
        Money todayAmount = getTodayWithdrawAmount();
        Money afterAmount = todayAmount.add(amount);
        if (settings.dailyWithdrawLimit() != null && afterAmount.isGreaterThan(settings.dailyWithdrawLimit())) {
            throw new IllegalArgumentException("超过每日提现限额");
        }
    }

    private Money calculateTransferFee(Money amount) {
        // 简化的手续费计算：转账金额的0.1%，最低0.01元，最高5元
        Money feeRate = amount.multiply(java.math.BigDecimal.valueOf(0.001));
        Money minFee = Money.cny(0.01);
        Money maxFee = Money.cny(5.00);

        if (feeRate.isLessThan(minFee)) {
            return minFee;
        }
        if (feeRate.isGreaterThan(maxFee)) {
            return maxFee;
        }
        return feeRate;
    }

    private Money calculateWithdrawFee(Money amount) {
        // 简化的提现手续费计算：固定2元
        return Money.cny(2.00);
    }

    private void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    // ========================================
    // Getter方法
    // ========================================

    public WalletId getWalletId() {
        return walletId;
    }

    public UserId getUserId() {
        return userId;
    }

    public Money getBalance() {
        return balance;
    }

    public Money getFrozenBalance() {
        return frozenBalance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public WalletSettings getSettings() {
        return settings;
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public LocalDateTime getLastTransactionTime() {
        return lastTransactionTime;
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
