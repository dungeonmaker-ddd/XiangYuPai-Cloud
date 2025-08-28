package com.xypai.user.domain.wallet.enums;

/**
 * 钱包状态枚举
 *
 * @author XyPai
 * @since 2025-01-02
 */
public enum WalletStatus {

    /**
     * 正常 - 钱包正常使用
     */
    ACTIVE("正常"),

    /**
     * 冻结 - 钱包被冻结，禁止交易
     */
    FROZEN("冻结"),

    /**
     * 限制 - 钱包受限，部分功能不可用
     */
    RESTRICTED("限制"),

    /**
     * 关闭 - 钱包已关闭
     */
    CLOSED("关闭"),

    /**
     * 审核中 - 钱包在审核中
     */
    UNDER_REVIEW("审核中");

    private final String description;

    WalletStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否可以进行交易
     */
    public boolean canTransact() {
        return this == ACTIVE;
    }

    /**
     * 检查是否可以接收资金
     */
    public boolean canReceive() {
        return this == ACTIVE || this == RESTRICTED;
    }

    /**
     * 检查是否可以发送资金
     */
    public boolean canSend() {
        return this == ACTIVE;
    }

    /**
     * 检查是否可以充值
     */
    public boolean canRecharge() {
        return this == ACTIVE;
    }

    /**
     * 检查是否可以提现
     */
    public boolean canWithdraw() {
        return this == ACTIVE;
    }
}
