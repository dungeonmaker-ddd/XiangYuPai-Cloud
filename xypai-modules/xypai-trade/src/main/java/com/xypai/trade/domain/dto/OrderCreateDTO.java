package com.xypai.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单创建DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单创建DTO")
public class OrderCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 卖家用户ID
     */
    @Schema(description = "卖家用户ID", required = true)
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    /**
     * 关联技能内容ID
     */
    @Schema(description = "关联技能内容ID", required = true)
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 订单金额(元)
     */
    @Schema(description = "订单金额(元)", required = true)
    @NotNull(message = "订单金额不能为空")
    @Positive(message = "订单金额必须大于0")
    private BigDecimal amount;

    /**
     * 服务时长(小时)
     */
    @Schema(description = "服务时长(小时)")
    @Positive(message = "服务时长必须大于0")
    private Integer duration;

    /**
     * 服务描述
     */
    @Schema(description = "服务描述", required = true)
    @NotBlank(message = "服务描述不能为空")
    @Size(max = 1000, message = "服务描述不能超过1000个字符")
    private String serviceDescription;

    /**
     * 服务要求
     */
    @Schema(description = "服务要求")
    @Size(max = 500, message = "服务要求不能超过500个字符")
    private String serviceRequirements;

    /**
     * 期望开始时间
     */
    @Schema(description = "期望开始时间")
    private String expectedStartTime;

    /**
     * 期望结束时间
     */
    @Schema(description = "期望结束时间")
    private String expectedEndTime;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式")
    @Size(max = 200, message = "联系方式不能超过200个字符")
    private String contactInfo;

    /**
     * 特殊要求
     */
    @Schema(description = "特殊要求")
    @Size(max = 500, message = "特殊要求不能超过500个字符")
    private String specialRequirements;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;
}
