package com.xypai.user.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * 💰 金额值对象
 * <p>
 * 封装货币金额，确保不可变性和精度
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record Money(
        BigDecimal amount,
        Currency currency
) {

    public Money {
        Objects.requireNonNull(amount, "金额不能为空");
        Objects.requireNonNull(currency, "币种不能为空");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }

        // 设置精度为2位小数
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 创建人民币金额
     */
    public static Money cny(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("CNY"));
    }

    /**
     * 创建人民币金额（从double）
     */
    public static Money cny(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("CNY"));
    }

    /**
     * 创建零金额
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO, Currency.getInstance("CNY"));
    }

    /**
     * 金额相加
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * 金额相减
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不足");
        }
        return new Money(result, this.currency);
    }

    /**
     * 金额乘法
     */
    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "乘数不能为空");
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("乘数不能为负数");
        }
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    /**
     * 金额除法
     */
    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "除数不能为空");
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("除数必须大于0");
        }
        return new Money(this.amount.divide(divisor, 2, RoundingMode.HALF_UP), this.currency);
    }

    /**
     * 比较金额大小
     */
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * 比较金额大小
     */
    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    /**
     * 比较金额大小
     */
    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * 比较金额大小
     */
    public boolean isLessThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }

    /**
     * 是否为零
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 是否为正数
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取金额（保持兼容性）
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 获取币种（保持兼容性）
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * 格式化显示
     */
    public String toFormattedString() {
        return currency.getSymbol() + amount.toString();
    }

    /**
     * 验证币种相同
     */
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("币种不匹配：" + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public String toString() {
        return toFormattedString();
    }
}
