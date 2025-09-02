package com.xypai.trade.controller.app;

import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.core.web.page.TableDataInfo;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.security.annotation.RequiresPermissions;
import com.xypai.trade.domain.dto.ServiceOrderAddDTO;
import com.xypai.trade.domain.dto.ServiceOrderQueryDTO;
import com.xypai.trade.domain.dto.ServiceOrderUpdateDTO;
import com.xypai.trade.domain.vo.ServiceOrderDetailVO;
import com.xypai.trade.domain.vo.ServiceOrderListVO;
import com.xypai.trade.service.IServiceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 服务订单控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "服务订单", description = "服务订单管理API")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class ServiceOrderController extends BaseController {

    private final IServiceOrderService serviceOrderService;

    /**
     * 查询订单列表
     */
    @Operation(summary = "查询订单列表", description = "分页查询订单列表信息")
    @GetMapping("/list")
    @RequiresPermissions("trade:order:list")
    public TableDataInfo list(ServiceOrderQueryDTO query) {
        startPage();
        List<ServiceOrderListVO> list = serviceOrderService.selectOrderList(query);
        return getDataTable(list);
    }

    /**
     * 获取订单详细信息
     */
    @Operation(summary = "获取订单详细信息", description = "根据订单ID获取详细信息")
    @GetMapping("/{orderId}")
    @RequiresPermissions("trade:order:query")
    @Log(title = "订单管理", businessType = BusinessType.QUERY)
    public R<ServiceOrderDetailVO> getInfo(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        return R.ok(serviceOrderService.selectOrderById(orderId));
    }

    /**
     * 创建订单
     */
    @Operation(summary = "创建订单", description = "创建新的服务订单")
    @PostMapping
    @RequiresPermissions("trade:order:add")
    @Log(title = "订单管理", businessType = BusinessType.INSERT)
    public R<Void> add(@Validated @RequestBody ServiceOrderAddDTO orderAddDTO) {
        return toAjax(serviceOrderService.createOrder(orderAddDTO));
    }

    /**
     * 修改订单
     */
    @Operation(summary = "修改订单", description = "更新订单信息")
    @PutMapping
    @RequiresPermissions("trade:order:edit")
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@Validated @RequestBody ServiceOrderUpdateDTO orderUpdateDTO) {
        return toAjax(serviceOrderService.updateOrder(orderUpdateDTO));
    }

    /**
     * 删除订单
     */
    @Operation(summary = "删除订单", description = "根据订单ID删除订单")
    @DeleteMapping("/{orderIds}")
    @RequiresPermissions("trade:order:remove")
    @Log(title = "订单管理", businessType = BusinessType.DELETE)
    public R<Void> remove(
            @Parameter(description = "订单ID数组", required = true)
            @PathVariable Long[] orderIds) {
        return toAjax(serviceOrderService.deleteOrderByIds(Arrays.asList(orderIds)));
    }

    /**
     * 支付订单
     */
    @Operation(summary = "支付订单", description = "支付指定订单")
    @PostMapping("/{orderId}/pay")
    @RequiresPermissions("trade:order:pay")
    @Log(title = "订单支付", businessType = BusinessType.UPDATE)
    public R<String> payOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "支付方式")
            @RequestParam(defaultValue = "wallet") String paymentMethod) {
        return R.ok("订单支付成功", serviceOrderService.payOrder(orderId, paymentMethod));
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单", description = "取消指定订单")
    @PutMapping("/{orderId}/cancel")
    @RequiresPermissions("trade:order:edit")
    @Log(title = "取消订单", businessType = BusinessType.UPDATE)
    public R<Void> cancelOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "取消原因")
            @RequestParam(required = false) String reason) {
        return toAjax(serviceOrderService.cancelOrder(orderId, reason));
    }

    /**
     * 确认开始服务
     */
    @Operation(summary = "确认开始服务", description = "卖家确认开始提供服务")
    @PutMapping("/{orderId}/start-service")
    @RequiresPermissions("trade:order:edit")
    @Log(title = "开始服务", businessType = BusinessType.UPDATE)
    public R<Void> startService(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        return toAjax(serviceOrderService.startService(orderId));
    }

    /**
     * 确认完成服务
     */
    @Operation(summary = "确认完成服务", description = "买家确认服务完成")
    @PutMapping("/{orderId}/complete")
    @RequiresPermissions("trade:order:edit")
    @Log(title = "完成订单", businessType = BusinessType.UPDATE)
    public R<Void> completeOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "评价分数")
            @RequestParam(required = false) Integer rating,
            @Parameter(description = "评价内容")
            @RequestParam(required = false) String review) {
        return toAjax(serviceOrderService.completeOrder(orderId, rating, review));
    }

    /**
     * 申请退款
     */
    @Operation(summary = "申请退款", description = "买家申请订单退款")
    @PostMapping("/{orderId}/refund")
    @RequiresPermissions("trade:order:refund")
    @Log(title = "申请退款", businessType = BusinessType.UPDATE)
    public R<Void> requestRefund(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "退款原因", required = true)
            @RequestParam String reason) {
        return toAjax(serviceOrderService.requestRefund(orderId, reason));
    }

    /**
     * 处理退款
     */
    @Operation(summary = "处理退款", description = "管理员处理退款申请")
    @PutMapping("/{orderId}/handle-refund")
    @RequiresPermissions("trade:order:admin")
    @Log(title = "处理退款", businessType = BusinessType.UPDATE)
    public R<Void> handleRefund(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "是否同意退款", required = true)
            @RequestParam Boolean approved,
            @Parameter(description = "处理说明")
            @RequestParam(required = false) String note) {
        return toAjax(serviceOrderService.handleRefund(orderId, approved, note));
    }

    /**
     * 获取我的购买订单
     */
    @Operation(summary = "获取我的购买订单", description = "获取当前用户作为买家的订单列表")
    @GetMapping("/my-purchases")
    @RequiresPermissions("trade:order:query")
    public TableDataInfo getMyPurchases(
            @Parameter(description = "订单状态")
            @RequestParam(required = false) Integer status) {
        startPage();
        List<ServiceOrderListVO> list = serviceOrderService.getMyPurchaseOrders(status);
        return getDataTable(list);
    }

    /**
     * 获取我的销售订单
     */
    @Operation(summary = "获取我的销售订单", description = "获取当前用户作为卖家的订单列表")
    @GetMapping("/my-sales")
    @RequiresPermissions("trade:order:query")
    public TableDataInfo getMySales(
            @Parameter(description = "订单状态")
            @RequestParam(required = false) Integer status) {
        startPage();
        List<ServiceOrderListVO> list = serviceOrderService.getMySaleOrders(status);
        return getDataTable(list);
    }

    /**
     * 获取订单统计
     */
    @Operation(summary = "获取订单统计", description = "获取订单统计数据")
    @GetMapping("/statistics")
    @RequiresPermissions("trade:order:query")
    public R<Map<String, Object>> getOrderStatistics(
            @Parameter(description = "统计开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "统计结束时间")
            @RequestParam(required = false) String endDate) {
        return R.ok(serviceOrderService.getOrderStatistics(startDate, endDate));
    }

    /**
     * 获取订单状态统计
     */
    @Operation(summary = "获取订单状态统计", description = "获取各状态订单数量统计")
    @GetMapping("/status-statistics")
    @RequiresPermissions("trade:order:query")
    public R<Map<String, Long>> getOrderStatusStatistics() {
        return R.ok(serviceOrderService.getOrderStatusStatistics());
    }

    /**
     * 获取热门服务
     */
    @Operation(summary = "获取热门服务", description = "获取订单量最多的服务")
    @GetMapping("/popular-services")
    @RequiresPermissions("trade:order:query")
    public TableDataInfo getPopularServices(
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        startPage();
        List<Map<String, Object>> list = serviceOrderService.getPopularServices(limit);
        return getDataTable(list);
    }

    /**
     * 批量处理订单
     */
    @Operation(summary = "批量处理订单", description = "批量更新订单状态")
    @PutMapping("/batch-update")
    @RequiresPermissions("trade:order:admin")
    @Log(title = "批量处理订单", businessType = BusinessType.UPDATE)
    public R<Void> batchUpdateOrders(
            @Parameter(description = "订单ID列表", required = true)
            @RequestBody List<Long> orderIds,
            @Parameter(description = "目标状态", required = true)
            @RequestParam Integer targetStatus,
            @Parameter(description = "处理说明")
            @RequestParam(required = false) String note) {
        return toAjax(serviceOrderService.batchUpdateOrders(orderIds, targetStatus, note));
    }

    /**
     * 导出订单数据
     */
    @Operation(summary = "导出订单数据", description = "导出订单列表到Excel")
    @PostMapping("/export")
    @RequiresPermissions("trade:order:export")
    @Log(title = "导出订单", businessType = BusinessType.EXPORT)
    public R<String> exportOrders(ServiceOrderQueryDTO query) {
        return R.ok("导出成功", serviceOrderService.exportOrders(query));
    }
}
