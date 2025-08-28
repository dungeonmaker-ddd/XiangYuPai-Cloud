package com.xypai.user.domain.wallet.enums;

/**
 * 交易类型枚举
 *
 * @author XyPai
 * @since 2025-01-02
 */
public enum TransactionType {

    /**
     * 充值 - 向钱包充值
     */
    RECHARGE("充值", true),

    /**
     * 提现 - 从钱包提现
     */
    WITHDRAW("提现", false),

    /**
     * 转账发送 - 向其他用户转账
     */
    TRANSFER_OUT("转账发送", false),

    /**
     * 转账接收 - 接收其他用户转账
     */
    TRANSFER_IN("转账接收", true),

    /**
     * 支付 - 购买商品或服务
     */
    PAYMENT("支付", false),

    /**
     * 退款 - 收到退款
     */
    REFUND("退款", true),

    /**
     * 奖励 - 系统奖励
     */
    REWARD("奖励", true),

    /**
     * 红包发送 - 发送红包
     */
    RED_PACKET_SEND("红包发送", false),

    /**
     * 红包接收 - 接收红包
     */
    RED_PACKET_RECEIVE("红包接收", true),

    /**
     * 手续费 - 交易手续费
     */
    FEE("手续费", false),

    /**
     * 调整 - 管理员调整
     */
    ADJUSTMENT("调整", true);

    private final String description;
    private final boolean isIncome; // true表示收入，false表示支出

    TransactionType(String description, boolean isIncome) {
        this.description = description;
        this.isIncome = isIncome;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 是否为收入类型
     */
    public boolean isIncome() {
        return isIncome;
    }

    /**
     * 是否为支出类型
     */
    public boolean isExpense() {
        return !isIncome;
    }

    /**
     * 是否为转账类型
     */
    public boolean isTransfer() {
        return this == TRANSFER_OUT || this == TRANSFER_IN;
    }

    /**
     * 是否为红包类型
     */
    public boolean isRedPacket() {
        return this == RED_PACKET_SEND || this == RED_PACKET_RECEIVE;
    }

    /**
     * 是否需要手续费
     */
    public boolean requiresFee() {
        return this == WITHDRAW || this == TRANSFER_OUT;
    }

    /**
     * 获取对应的反向交易类型
     */
    public TransactionType getOpposite() {
        return switch (this) {
            case TRANSFER_OUT -> TRANSFER_IN;
            case TRANSFER_IN -> TRANSFER_OUT;
            case RED_PACKET_SEND -> RED_PACKET_RECEIVE;
            case RED_PACKET_RECEIVE -> RED_PACKET_SEND;
            case PAYMENT -> REFUND;
            case REFUND -> PAYMENT;
            default -> throw new IllegalStateException("交易类型 " + this + " 没有对应的反向类型");
        };
    }
}
