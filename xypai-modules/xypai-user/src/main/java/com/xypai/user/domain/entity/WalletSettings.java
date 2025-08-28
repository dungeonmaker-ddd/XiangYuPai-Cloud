package com.xypai.user.domain.entity;

import com.xypai.user.domain.valueobject.Money;

/**
 * 钱包设置实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record WalletSettings(
        boolean enableTransfer,         // 是否启用转账功能
        boolean enablePayment,          // 是否启用支付功能
        boolean enableWithdraw,         // 是否启用提现功能
        Money dailyTransferLimit,       // 每日转账限额
        Money dailyPaymentLimit,        // 每日支付限额
        Money dailyWithdrawLimit,       // 每日提现限额
        Money singleTransferLimit,      // 单笔转账限额
        Money singlePaymentLimit,       // 单笔支付限额
        Money singleWithdrawLimit,      // 单笔提现限额
        boolean requirePasswordForTransfer,  // 转账是否需要支付密码
        boolean requirePasswordForPayment,   // 支付是否需要支付密码
        boolean requirePasswordForWithdraw,  // 提现是否需要支付密码
        boolean enableSmsNotification,       // 是否启用短信通知
        boolean enableEmailNotification,     // 是否启用邮件通知
        boolean enableAppNotification,       // 是否启用App通知
        int autoLockMinutes,                 // 自动锁定时间（分钟）
        boolean enableRiskControl           // 是否启用风控
) {

    public WalletSettings {
        // 验证限额设置
        if (dailyTransferLimit != null && dailyTransferLimit.isLessThan(Money.zeroCny())) {
            throw new IllegalArgumentException("每日转账限额不能为负数");
        }
        if (dailyPaymentLimit != null && dailyPaymentLimit.isLessThan(Money.zeroCny())) {
            throw new IllegalArgumentException("每日支付限额不能为负数");
        }
        if (dailyWithdrawLimit != null && dailyWithdrawLimit.isLessThan(Money.zeroCny())) {
            throw new IllegalArgumentException("每日提现限额不能为负数");
        }

        if (singleTransferLimit != null && singleTransferLimit.isLessThan(Money.zeroCny())) {
            throw new IllegalArgumentException("单笔转账限额不能为负数");
        }
        if (singlePaymentLimit != null && singlePaymentLimit.isLessThan(Money.zeroCny())) {
            throw new IllegalArgumentException("单笔支付限额不能为负数");
        }
        if (singleWithdrawLimit != null && singleWithdrawLimit.isLessThan(Money.zeroCny())) {
            throw new IllegalArgumentException("单笔提现限额不能为负数");
        }

        // 验证自动锁定时间
        if (autoLockMinutes < 0 || autoLockMinutes > 1440) { // 最多24小时
            throw new IllegalArgumentException("自动锁定时间必须在0-1440分钟之间");
        }
    }

    /**
     * 创建默认设置
     */
    public static WalletSettings defaultSettings() {
        return new WalletSettings(
                true,  // enableTransfer
                true,  // enablePayment
                true,  // enableWithdraw
                Money.cny(10000),  // dailyTransferLimit 每日转账限额1万
                Money.cny(5000),   // dailyPaymentLimit 每日支付限额5千
                Money.cny(2000),   // dailyWithdrawLimit 每日提现限额2千
                Money.cny(5000),   // singleTransferLimit 单笔转账限额5千
                Money.cny(2000),   // singlePaymentLimit 单笔支付限额2千
                Money.cny(1000),   // singleWithdrawLimit 单笔提现限额1千
                true,  // requirePasswordForTransfer
                true,  // requirePasswordForPayment
                true,  // requirePasswordForWithdraw
                true,  // enableSmsNotification
                true,  // enableEmailNotification
                true,  // enableAppNotification
                30,    // autoLockMinutes 30分钟自动锁定
                true   // enableRiskControl
        );
    }

    /**
     * 创建高安全设置
     */
    public static WalletSettings highSecuritySettings() {
        return new WalletSettings(
                true,  // enableTransfer
                true,  // enablePayment
                true,  // enableWithdraw
                Money.cny(5000),   // dailyTransferLimit 每日转账限额5千
                Money.cny(2000),   // dailyPaymentLimit 每日支付限额2千
                Money.cny(1000),   // dailyWithdrawLimit 每日提现限额1千
                Money.cny(1000),   // singleTransferLimit 单笔转账限额1千
                Money.cny(500),    // singlePaymentLimit 单笔支付限额500
                Money.cny(500),    // singleWithdrawLimit 单笔提现限额500
                true,  // requirePasswordForTransfer
                true,  // requirePasswordForPayment
                true,  // requirePasswordForWithdraw
                true,  // enableSmsNotification
                true,  // enableEmailNotification
                true,  // enableAppNotification
                15,    // autoLockMinutes 15分钟自动锁定
                true   // enableRiskControl
        );
    }

    /**
     * 创建低限额设置
     */
    public static WalletSettings lowLimitSettings() {
        return new WalletSettings(
                true,  // enableTransfer
                true,  // enablePayment
                false, // enableWithdraw 禁用提现
                Money.cny(1000),   // dailyTransferLimit 每日转账限额1千
                Money.cny(500),    // dailyPaymentLimit 每日支付限额500
                Money.zeroCny(),   // dailyWithdrawLimit 提现限额为0
                Money.cny(200),    // singleTransferLimit 单笔转账限额200
                Money.cny(100),    // singlePaymentLimit 单笔支付限额100
                Money.zeroCny(),   // singleWithdrawLimit 单笔提现限额为0
                false, // requirePasswordForTransfer
                false, // requirePasswordForPayment
                false, // requirePasswordForWithdraw
                false, // enableSmsNotification
                false, // enableEmailNotification
                true,  // enableAppNotification
                60,    // autoLockMinutes 60分钟自动锁定
                true   // enableRiskControl
        );
    }

    /**
     * 检查是否允许转账
     */
    public boolean allowsTransfer(Money amount) {
        return enableTransfer &&
                (singleTransferLimit == null || amount.isLessThanOrEqualTo(singleTransferLimit));
    }

    /**
     * 检查是否允许支付
     */
    public boolean allowsPayment(Money amount) {
        return enablePayment &&
                (singlePaymentLimit == null || amount.isLessThanOrEqualTo(singlePaymentLimit));
    }

    /**
     * 检查是否允许提现
     */
    public boolean allowsWithdraw(Money amount) {
        return enableWithdraw &&
                (singleWithdrawLimit == null || amount.isLessThanOrEqualTo(singleWithdrawLimit));
    }

    /**
     * 更新转账设置
     */
    public WalletSettings withTransferSettings(boolean enable, Money dailyLimit, Money singleLimit) {
        return new WalletSettings(
                enable,
                this.enablePayment,
                this.enableWithdraw,
                dailyLimit,
                this.dailyPaymentLimit,
                this.dailyWithdrawLimit,
                singleLimit,
                this.singlePaymentLimit,
                this.singleWithdrawLimit,
                this.requirePasswordForTransfer,
                this.requirePasswordForPayment,
                this.requirePasswordForWithdraw,
                this.enableSmsNotification,
                this.enableEmailNotification,
                this.enableAppNotification,
                this.autoLockMinutes,
                this.enableRiskControl
        );
    }

    /**
     * 更新安全设置
     */
    public WalletSettings withSecuritySettings(
            boolean requirePasswordForTransfer,
            boolean requirePasswordForPayment,
            boolean requirePasswordForWithdraw,
            int autoLockMinutes
    ) {
        return new WalletSettings(
                this.enableTransfer,
                this.enablePayment,
                this.enableWithdraw,
                this.dailyTransferLimit,
                this.dailyPaymentLimit,
                this.dailyWithdrawLimit,
                this.singleTransferLimit,
                this.singlePaymentLimit,
                this.singleWithdrawLimit,
                requirePasswordForTransfer,
                requirePasswordForPayment,
                requirePasswordForWithdraw,
                this.enableSmsNotification,
                this.enableEmailNotification,
                this.enableAppNotification,
                autoLockMinutes,
                this.enableRiskControl
        );
    }
}
