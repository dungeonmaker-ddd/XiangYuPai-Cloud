package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 用户钱包实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_wallet")
public class UserWallet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联用户ID
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 余额(分为单位,避免精度问题)
     */
    @TableField("balance")
    @Builder.Default
    private Long balance = 0L;

    /**
     * 乐观锁版本号(并发控制)
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    /**
     * 获取余额(元)
     */
    public BigDecimal getBalanceYuan() {
        return BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 设置余额(元)
     */
    public void setBalanceYuan(BigDecimal balanceYuan) {
        this.balance = balanceYuan.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 增加余额(分)
     */
    public void addBalance(Long amount) {
        if (amount != null && amount > 0) {
            this.balance += amount;
        }
    }

    /**
     * 减少余额(分)
     */
    public boolean deductBalance(Long amount) {
        if (amount != null && amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * 检查余额是否足够
     */
    public boolean hasEnoughBalance(Long amount) {
        return amount != null && this.balance >= amount;
    }

    /**
     * 检查是否有余额
     */
    public boolean hasBalance() {
        return this.balance > 0;
    }

    /**
     * 格式化余额显示
     */
    public String getFormattedBalance() {
        return "¥" + getBalanceYuan().toString();
    }
}
