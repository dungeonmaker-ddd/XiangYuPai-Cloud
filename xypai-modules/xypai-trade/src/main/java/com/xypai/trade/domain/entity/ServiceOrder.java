package com.xypai.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 服务订单实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "service_order", autoResultMap = true)
public class ServiceOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单唯一ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 买家用户ID
     */
    @TableField("buyer_id")
    @NotNull(message = "买家ID不能为空")
    private Long buyerId;

    /**
     * 卖家用户ID
     */
    @TableField("seller_id")
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    /**
     * 关联技能内容ID
     */
    @TableField("content_id")
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 订单金额(分)
     */
    @TableField("amount")
    @NotNull(message = "订单金额不能为空")
    @Positive(message = "订单金额必须大于0")
    private Long amount;

    /**
     * 服务时长(小时)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 订单状态(0=待付款,1=已付款,2=服务中,3=已完成,4=已取消,5=已退款)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 0;

    /**
     * 订单扩展信息JSON
     */
    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> data;

    /**
     * 下单时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    /**
     * 订单状态枚举
     */
    public enum Status {
        PENDING_PAYMENT(0, "待付款"),
        PAID(1, "已付款"),
        IN_SERVICE(2, "服务中"),
        COMPLETED(3, "已完成"),
        CANCELLED(4, "已取消"),
        REFUNDED(5, "已退款");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 获取订单金额(元)
     */
    public BigDecimal getAmountYuan() {
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 设置订单金额(元)
     */
    public void setAmountYuan(BigDecimal amountYuan) {
        this.amount = amountYuan.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 是否为待付款状态
     */
    public boolean isPendingPayment() {
        return Status.PENDING_PAYMENT.getCode().equals(this.status);
    }

    /**
     * 是否已付款
     */
    public boolean isPaid() {
        return Status.PAID.getCode().equals(this.status);
    }

    /**
     * 是否服务中
     */
    public boolean isInService() {
        return Status.IN_SERVICE.getCode().equals(this.status);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return Status.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return Status.CANCELLED.getCode().equals(this.status);
    }

    /**
     * 是否已退款
     */
    public boolean isRefunded() {
        return Status.REFUNDED.getCode().equals(this.status);
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status orderStatus = Status.fromCode(this.status);
        return orderStatus != null ? orderStatus.getDesc() : "未知";
    }

    /**
     * 格式化金额显示
     */
    public String getFormattedAmount() {
        return "¥" + getAmountYuan().toString();
    }

    /**
     * 获取订单编号（格式化显示）
     */
    public String getOrderNo() {
        return "SO" + id;
    }

    /**
     * 检查是否可以取消
     */
    public boolean canCancel() {
        return isPendingPayment();
    }

    /**
     * 检查是否可以退款
     */
    public boolean canRefund() {
        return isPaid() || isInService();
    }

    /**
     * 检查是否可以确认完成
     */
    public boolean canComplete() {
        return isInService();
    }

    /**
     * 获取服务描述
     */
    public String getServiceDescription() {
        return data != null ? (String) data.get("service_description") : null;
    }

    /**
     * 设置服务描述
     */
    public void setServiceDescription(String description) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put("service_description", description);
    }

    /**
     * 获取服务要求
     */
    public String getServiceRequirements() {
        return data != null ? (String) data.get("service_requirements") : null;
    }

    /**
     * 设置服务要求
     */
    public void setServiceRequirements(String requirements) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put("service_requirements", requirements);
    }
}
