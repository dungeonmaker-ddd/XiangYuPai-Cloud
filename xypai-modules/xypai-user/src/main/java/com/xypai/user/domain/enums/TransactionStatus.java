package com.xypai.user.domain.enums;

/**
 * 交易状态枚举
 *
 * @author XyPai
 * @since 2025-01-02
 */
public enum TransactionStatus {

    /**
     * 待处理 - 交易已创建，等待处理
     */
    PENDING("待处理"),

    /**
     * 处理中 - 交易正在处理
     */
    PROCESSING("处理中"),

    /**
     * 成功 - 交易已成功完成
     */
    SUCCESS("成功"),

    /**
     * 失败 - 交易处理失败
     */
    FAILED("失败"),

    /**
     * 已取消 - 交易被取消
     */
    CANCELLED("已取消"),

    /**
     * 已退款 - 交易已退款
     */
    REFUNDED("已退款");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查交易是否为最终状态
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * 检查交易是否正在进行中
     */
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING;
    }

    /**
     * 检查交易是否成功
     */
    public boolean isSuccessful() {
        return this == SUCCESS;
    }

    /**
     * 检查交易是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING;
    }

    /**
     * 检查交易是否可以重试
     */
    public boolean canRetry() {
        return this == FAILED;
    }
}
