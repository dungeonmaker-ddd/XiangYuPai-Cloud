package com.xypai.user.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金额值对象
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "金额不能为空");
        Objects.requireNonNull(currency, "货币类型不能为空");

        if (amount.scale() > 2) {
            throw new IllegalArgumentException("金额精度不能超过2位小数");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }
        if (currency.trim().isEmpty()) {
            throw new IllegalArgumentException("货币类型不能为空");
        }
    }

    /**
     * 创建人民币金额
     */
    public static Money cny(BigDecimal amount) {
        return new Money(amount.setScale(2, RoundingMode.HALF_UP), "CNY");
    }

    /**
     * 创建人民币金额（从double）
     */
    public static Money cny(double amount) {
        return cny(BigDecimal.valueOf(amount));
    }

    /**
     * 创建人民币金额（从字符串）
     */
    public static Money cny(String amount) {
        return cny(new BigDecimal(amount));
    }

    /**
     * 创建零金额
     */
    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), currency);
    }

    /**
     * 创建零人民币
     */
    public static Money zeroCny() {
        return zero("CNY");
    }

    /**
     * 从分创建金额
     */
    public static Money fromCents(long cents, String currency) {
        BigDecimal amount = BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new Money(amount, currency);
    }

    /**
     * 加法运算
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(
                this.amount.add(other.amount).setScale(2, RoundingMode.HALF_UP),
                this.currency
        );
    }

    /**
     * 减法运算
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount).setScale(2, RoundingMode.HALF_UP);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("余额不足，无法执行减法运算");
        }
        return new Money(result, this.currency);
    }

    /**
     * 乘法运算
     */
    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "乘数不能为空");
        return new Money(
                this.amount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP),
                this.currency
        );
    }

    /**
     * 除法运算
     */
    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "除数不能为空");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("除数不能为零");
        }
        return new Money(
                this.amount.divide(divisor, 2, RoundingMode.HALF_UP),
                this.currency
        );
    }

    /**
     * 比较金额大小
     */
    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    /**
     * 检查是否大于
     */
    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    /**
     * 检查是否大于等于
     */
    public boolean isGreaterThanOrEqualTo(Money other) {
        return compareTo(other) >= 0;
    }

    /**
     * 检查是否小于
     */
    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    /**
     * 检查是否小于等于
     */
    public boolean isLessThanOrEqualTo(Money other) {
        return compareTo(other) <= 0;
    }

    /**
     * 检查是否等于
     */
    public boolean isEqualTo(Money other) {
        return compareTo(other) == 0;
    }

    /**
     * 检查是否为零
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 检查是否为正数
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取绝对值
     */
    public Money abs() {
        return new Money(amount.abs(), currency);
    }

    /**
     * 格式化显示
     */
    public String formatted() {
        return String.format("%s %.2f", currency, amount);
    }

    /**
     * 转换为分（避免浮点精度问题）
     */
    public long toCents() {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 验证货币类型相同
     */
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    String.format("货币类型不匹配: %s vs %s", this.currency, other.currency)
            );
        }
    }

    @Override
    public String toString() {
        return formatted();
    }
}
