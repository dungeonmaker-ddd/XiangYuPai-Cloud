package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 统一交易流水实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("transaction")
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 交易记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 交易金额(正负表示收支，分为单位)
     */
    @TableField("amount")
    @NotNull(message = "交易金额不能为空")
    private Long amount;

    /**
     * 交易类型
     */
    @TableField("type")
    @NotNull(message = "交易类型不能为空")
    private String type;

    /**
     * 关联业务ID(订单号/活动ID等)
     */
    @TableField("ref_id")
    private String refId;

    /**
     * 交易时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 交易类型枚举
     */
    public enum Type {
        RECHARGE("recharge", "充值"),
        CONSUME("consume", "消费"),
        REFUND("refund", "退款"),
        REWARD("reward", "奖励"),
        WITHDRAW("withdraw", "提现"),
        TRANSFER_IN("transfer_in", "转入"),
        TRANSFER_OUT("transfer_out", "转出");

        private final String code;
        private final String desc;

        Type(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Type fromCode(String code) {
            for (Type type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 获取交易金额(元)
     */
    public BigDecimal getAmountYuan() {
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 设置交易金额(元)
     */
    public void setAmountYuan(BigDecimal amountYuan) {
        this.amount = amountYuan.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 是否为收入交易
     */
    public boolean isIncome() {
        return amount > 0;
    }

    /**
     * 是否为支出交易
     */
    public boolean isExpense() {
        return amount < 0;
    }

    /**
     * 获取交易类型描述
     */
    public String getTypeDesc() {
        Type transactionType = Type.fromCode(this.type);
        return transactionType != null ? transactionType.getDesc() : this.type;
    }

    /**
     * 格式化金额显示
     */
    public String getFormattedAmount() {
        String prefix = isIncome() ? "+" : "";
        return prefix + "¥" + getAmountYuan().abs().toString();
    }
}
