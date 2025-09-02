package com.xypai.trade.service.impl;

import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.trade.domain.dto.PaymentDTO;
import com.xypai.trade.domain.dto.RefundDTO;
import com.xypai.trade.domain.entity.ServiceOrder;
import com.xypai.trade.domain.vo.PaymentResultVO;
import com.xypai.trade.mapper.ServiceOrderMapper;
import com.xypai.trade.service.IOrderService;
import com.xypai.trade.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务实现类
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final ServiceOrderMapper serviceOrderMapper;
    private final IOrderService orderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO createPayment(PaymentDTO paymentDTO) {
        // 验证订单
        ServiceOrder order = validatePaymentOrder(paymentDTO.getOrderId());
        
        // 根据支付方式处理
        switch (paymentDTO.getPaymentMethod().toLowerCase()) {
            case "wallet":
                return walletPay(paymentDTO.getOrderId(), paymentDTO.getPaymentPassword());
            case "wechat":
                return wechatPay(paymentDTO.getOrderId(), paymentDTO.getClientIp(), paymentDTO.getNotifyUrl());
            case "alipay":
                return alipayPay(paymentDTO.getOrderId(), paymentDTO.getClientIp(), 
                               paymentDTO.getNotifyUrl(), paymentDTO.getReturnUrl());
            default:
                throw new ServiceException("不支持的支付方式：" + paymentDTO.getPaymentMethod());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO walletPay(Long orderId, String paymentPassword) {
        ServiceOrder order = validatePaymentOrder(orderId);
        Long currentUserId = SecurityUtils.getUserId();
        
        if (currentUserId == null || !currentUserId.equals(order.getBuyerId())) {
            throw new ServiceException("无权限支付该订单");
        }

        // 验证支付密码
        if (!validatePaymentPassword(currentUserId, paymentPassword)) {
            return PaymentResultVO.builder()
                    .orderId(orderId)
                    .orderNo(order.getOrderNo())
                    .paymentStatus("failed")
                    .paymentMethod("wallet")
                    .errorCode("INVALID_PASSWORD")
                    .errorMessage("支付密码错误")
                    .build();
        }

        // 检查钱包余额
        if (!checkWalletBalance(currentUserId, order.getAmount())) {
            return PaymentResultVO.builder()
                    .orderId(orderId)
                    .orderNo(order.getOrderNo())
                    .paymentStatus("failed")
                    .paymentMethod("wallet")
                    .errorCode("INSUFFICIENT_BALANCE")
                    .errorMessage("钱包余额不足")
                    .build();
        }

        // 扣款
        boolean deductSuccess = deductWalletAmount(currentUserId, order.getAmount(), orderId, "订单支付");
        if (!deductSuccess) {
            return PaymentResultVO.builder()
                    .orderId(orderId)
                    .orderNo(order.getOrderNo())
                    .paymentStatus("failed")
                    .paymentMethod("wallet")
                    .errorCode("DEDUCT_FAILED")
                    .errorMessage("扣款失败")
                    .build();
        }

        // 生成支付流水号
        String paymentNo = generatePaymentNo(orderId, "wallet");
        
        // 处理支付成功
        boolean success = handlePaymentSuccess(orderId, paymentNo, null, "wallet");
        if (!success) {
            // 支付处理失败，退款
            refundWalletAmount(currentUserId, order.getAmount(), orderId, "支付处理失败退款");
            throw new ServiceException("支付处理失败");
        }

        // 记录支付日志
        recordPaymentLog(orderId, "wallet", "PAY", "钱包支付成功", "SUCCESS");

        return PaymentResultVO.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .paymentStatus("success")
                .paymentMethod("wallet")
                .paymentAmount(order.getAmountYuan())
                .paymentNo(paymentNo)
                .paymentTime(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResultVO wechatPay(Long orderId, String clientIp, String notifyUrl) {
        ServiceOrder order = validatePaymentOrder(orderId);
        
        // 生成支付流水号
        String paymentNo = generatePaymentNo(orderId, "wechat");
        
        // TODO: 集成微信支付API
        // 这里模拟微信支付流程
        
        // 记录支付日志
        recordPaymentLog(orderId, "wechat", "CREATE_ORDER", "创建微信支付订单", "PENDING");
        
        // 模拟返回微信支付二维码
        String qrCode = "weixin://wxpay/bizpayurl?pr=" + UUID.randomUUID().toString().replace("-", "");
        
        return PaymentResultVO.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .paymentStatus("pending")
                .paymentMethod("wechat")
                .paymentAmount(order.getAmountYuan())
                .paymentNo(paymentNo)
                .qrCode(qrCode)
                .needRedirect(false)
                .build();
    }

    @Override
    public PaymentResultVO alipayPay(Long orderId, String clientIp, String notifyUrl, String returnUrl) {
        ServiceOrder order = validatePaymentOrder(orderId);
        
        // 生成支付流水号
        String paymentNo = generatePaymentNo(orderId, "alipay");
        
        // TODO: 集成支付宝API
        // 这里模拟支付宝支付流程
        
        // 记录支付日志
        recordPaymentLog(orderId, "alipay", "CREATE_ORDER", "创建支付宝订单", "PENDING");
        
        // 模拟返回支付宝支付链接
        String paymentUrl = "https://openapi.alipay.com/gateway.do?service=create_partner_trade_by_buyer&_input_charset=utf-8&partner=xxx&out_trade_no=" + paymentNo;
        
        return PaymentResultVO.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .paymentStatus("pending")
                .paymentMethod("alipay")
                .paymentAmount(order.getAmountYuan())
                .paymentNo(paymentNo)
                .paymentUrl(paymentUrl)
                .needRedirect(true)
                .redirectUrl(paymentUrl)
                .build();
    }

    @Override
    public String queryPaymentStatus(Long orderId) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (order.isPaid()) {
            return "success";
        } else if (order.isPendingPayment()) {
            return "pending";
        } else if (order.isCancelled()) {
            return "cancelled";
        } else {
            return "unknown";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentCallback(String paymentMethod, Map<String, Object> callbackData) {
        // TODO: 根据不同支付方式处理回调
        log.info("处理支付回调，支付方式：{}，回调数据：{}", paymentMethod, callbackData);
        
        // 验证回调数据的真实性
        // 解析订单信息
        // 更新订单状态
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(Long orderId, String paymentNo, String thirdPartyNo, String paymentMethod) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.isPendingPayment()) {
            log.warn("订单状态异常，订单ID：{}，当前状态：{}", orderId, order.getStatus());
            return false;
        }

        // 更新订单状态为已付款
        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(orderId)
                .status(ServiceOrder.Status.PAID.getCode())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = serviceOrderMapper.updateById(updateOrder);
        if (result <= 0) {
            log.error("更新订单支付状态失败，订单ID：{}", orderId);
            return false;
        }

        // 记录订单日志
        orderService.recordOrderLog(orderId, "PAYMENT_SUCCESS", "支付成功", order.getBuyerId(), 
                                   String.format("支付方式：%s，流水号：%s", paymentMethod, paymentNo));

        log.info("处理支付成功，订单ID：{}，支付方式：{}，流水号：{}", orderId, paymentMethod, paymentNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentFailed(Long orderId, String errorCode, String errorMessage) {
        // 记录支付失败日志
        recordPaymentLog(orderId, "unknown", "PAYMENT_FAILED", 
                        String.format("支付失败，错误码：%s，错误信息：%s", errorCode, errorMessage), "FAILED");
        
        log.warn("处理支付失败，订单ID：{}，错误码：{}，错误信息：{}", orderId, errorCode, errorMessage);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO applyRefund(RefundDTO refundDTO) {
        ServiceOrder order = serviceOrderMapper.selectById(refundDTO.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.canRefund()) {
            throw new ServiceException("订单当前状态不支持退款");
        }

        Long currentUserId = SecurityUtils.getUserId();
        
        // 权限验证
        if (currentUserId != null && !orderService.validateOrderPermission(refundDTO.getOrderId(), currentUserId, true, true)) {
            throw new ServiceException("无权限申请退款");
        }

        // 计算退款金额
        BigDecimal refundAmount = refundDTO.getRefundAmount() != null ? 
                                 refundDTO.getRefundAmount() : order.getAmountYuan();
        
        if (refundAmount.compareTo(order.getAmountYuan()) > 0) {
            throw new ServiceException("退款金额不能超过订单金额");
        }

        // 处理退款（这里简化为直接退款到钱包）
        Long refundAmountFen = refundAmount.multiply(BigDecimal.valueOf(100)).longValue();
        boolean refundSuccess = refundWalletAmount(order.getBuyerId(), refundAmountFen, 
                                                  refundDTO.getOrderId(), "订单退款：" + refundDTO.getRefundReason());
        
        if (!refundSuccess) {
            throw new ServiceException("退款处理失败");
        }

        // 更新订单状态
        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(refundDTO.getOrderId())
                .status(ServiceOrder.Status.REFUNDED.getCode())
                .updatedAt(LocalDateTime.now())
                .build();

        serviceOrderMapper.updateById(updateOrder);

        // 记录订单日志
        orderService.recordOrderLog(refundDTO.getOrderId(), "REFUND", "申请退款", currentUserId, 
                                   String.format("退款原因：%s，退款金额：%s", refundDTO.getRefundReason(), refundAmount));

        // 记录支付日志
        recordPaymentLog(refundDTO.getOrderId(), "wallet", "REFUND", "退款成功", "SUCCESS");

        return PaymentResultVO.builder()
                .orderId(refundDTO.getOrderId())
                .orderNo(order.getOrderNo())
                .paymentStatus("success")
                .paymentMethod("refund")
                .paymentAmount(refundAmount)
                .paymentTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processRefund(Long orderId, Boolean approved, String processNote) {
        // TODO: 实现退款审核处理
        log.info("处理退款审核，订单ID：{}，审核结果：{}，处理说明：{}", orderId, approved, processNote);
        return true;
    }

    @Override
    public String queryRefundStatus(Long orderId) {
        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            return "unknown";
        }

        return order.isRefunded() ? "success" : "pending";
    }

    @Override
    public boolean validatePaymentPassword(Long userId, String paymentPassword) {
        if (userId == null || StringUtils.isBlank(paymentPassword)) {
            return false;
        }

        // TODO: 调用用户服务验证支付密码
        // 这里模拟验证逻辑
        return "123456".equals(paymentPassword); // 简化验证
    }

    @Override
    public boolean checkWalletBalance(Long userId, Long amount) {
        if (userId == null || amount == null || amount <= 0) {
            return false;
        }

        // TODO: 调用钱包服务检查余额
        // 这里模拟检查逻辑
        return true; // 简化为总是有足够余额
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductWalletAmount(Long userId, Long amount, Long orderId, String description) {
        if (userId == null || amount == null || amount <= 0) {
            return false;
        }

        // TODO: 调用钱包服务扣款
        log.info("钱包扣款，用户ID：{}，金额：{}分，订单ID：{}，描述：{}", userId, amount, orderId, description);
        return true; // 简化为扣款成功
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundWalletAmount(Long userId, Long amount, Long orderId, String description) {
        if (userId == null || amount == null || amount <= 0) {
            return false;
        }

        // TODO: 调用钱包服务退款
        log.info("钱包退款，用户ID：{}，金额：{}分，订单ID：{}，描述：{}", userId, amount, orderId, description);
        return true; // 简化为退款成功
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferToSeller(Long sellerId, Long amount, Long orderId, String description) {
        if (sellerId == null || amount == null || amount <= 0) {
            return false;
        }

        // TODO: 调用钱包服务转账给卖家
        log.info("转账给卖家，卖家ID：{}，金额：{}分，订单ID：{}，描述：{}", sellerId, amount, orderId, description);
        return true; // 简化为转账成功
    }

    @Override
    public Map<String, Object> getPaymentConfig(String paymentMethod) {
        Map<String, Object> config = new HashMap<>();
        
        switch (paymentMethod.toLowerCase()) {
            case "wechat":
                config.put("appId", "wx_app_id");
                config.put("mchId", "wx_mch_id");
                config.put("enabled", true);
                break;
            case "alipay":
                config.put("appId", "alipay_app_id");
                config.put("partnerId", "alipay_partner_id");
                config.put("enabled", true);
                break;
            case "wallet":
                config.put("enabled", true);
                config.put("requirePassword", true);
                break;
            default:
                config.put("enabled", false);
                break;
        }
        
        return config;
    }

    @Override
    public String generatePaymentNo(Long orderId, String paymentMethod) {
        String prefix = "";
        switch (paymentMethod.toLowerCase()) {
            case "wechat":
                prefix = "WX";
                break;
            case "alipay":
                prefix = "AL";
                break;
            case "wallet":
                prefix = "WL";
                break;
            default:
                prefix = "UN";
                break;
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + timestamp + orderId;
    }

    @Override
    public boolean recordPaymentLog(Long orderId, String paymentMethod, String actionType, String actionDesc, String result) {
        // TODO: 实现支付日志记录
        log.info("支付日志 - 订单ID：{}，支付方式：{}，操作：{}，描述：{}，结果：{}", 
                orderId, paymentMethod, actionType, actionDesc, result);
        return true;
    }

    /**
     * 验证支付订单
     */
    private ServiceOrder validatePaymentOrder(Long orderId) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.isPendingPayment()) {
            throw new ServiceException("订单状态不正确，无法支付");
        }

        return order;
    }
}
