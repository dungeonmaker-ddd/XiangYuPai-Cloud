package com.xypai.user.controller.app;

import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.core.web.page.TableDataInfo;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.security.annotation.RequiresPermissions;
import com.xypai.user.domain.dto.TransactionQueryDTO;
import com.xypai.user.domain.dto.WalletRechargeDTO;
import com.xypai.user.domain.dto.WalletTransferDTO;
import com.xypai.user.domain.vo.TransactionVO;
import com.xypai.user.domain.vo.UserWalletVO;
import com.xypai.user.service.IUserWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户钱包控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "用户钱包", description = "用户钱包管理API")
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Validated
public class UserWalletController extends BaseController {

    private final IUserWalletService userWalletService;

    /**
     * 获取用户钱包信息
     */
    @Operation(summary = "获取钱包信息", description = "获取当前用户的钱包详细信息")
    @GetMapping("/info")
    @RequiresPermissions("user:wallet:query")
    public R<UserWalletVO> getWalletInfo() {
        return R.ok(userWalletService.getUserWallet());
    }

    /**
     * 获取指定用户钱包信息
     */
    @Operation(summary = "获取指定用户钱包信息", description = "管理员查看指定用户钱包信息")
    @GetMapping("/{userId}")
    @RequiresPermissions("user:wallet:admin")
    public R<UserWalletVO> getUserWalletInfo(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        return R.ok(userWalletService.getUserWalletByUserId(userId));
    }

    /**
     * 钱包充值
     */
    @Operation(summary = "钱包充值", description = "用户钱包余额充值")
    @PostMapping("/recharge")
    @RequiresPermissions("user:wallet:recharge")
    @Log(title = "钱包充值", businessType = BusinessType.INSERT)
    public R<String> recharge(@Validated @RequestBody WalletRechargeDTO rechargeDTO) {
        return R.ok("充值订单创建成功", userWalletService.createRechargeOrder(rechargeDTO));
    }

    /**
     * 钱包转账
     */
    @Operation(summary = "钱包转账", description = "向其他用户转账")
    @PostMapping("/transfer")
    @RequiresPermissions("user:wallet:transfer")
    @Log(title = "钱包转账", businessType = BusinessType.INSERT)
    public R<Void> transfer(@Validated @RequestBody WalletTransferDTO transferDTO) {
        return R.result(userWalletService.transferMoney(transferDTO), "转账成功", "转账失败");
    }

    /**
     * 获取交易记录
     */
    @Operation(summary = "获取交易记录", description = "分页查询用户交易流水")
    @GetMapping("/transactions")
    @RequiresPermissions("user:wallet:query")
    public TableDataInfo getTransactions(TransactionQueryDTO query) {
        startPage();
        List<TransactionVO> list = userWalletService.getUserTransactions(query);
        return getDataTable(list);
    }

    /**
     * 获取交易详情
     */
    @Operation(summary = "获取交易详情", description = "根据交易ID获取详细信息")
    @GetMapping("/transactions/{transactionId}")
    @RequiresPermissions("user:wallet:query")
    public R<TransactionVO> getTransactionDetail(
            @Parameter(description = "交易ID", required = true)
            @PathVariable Long transactionId) {
        return R.ok(userWalletService.getTransactionById(transactionId));
    }

    /**
     * 获取钱包统计信息
     */
    @Operation(summary = "获取钱包统计", description = "获取用户钱包统计数据")
    @GetMapping("/statistics")
    @RequiresPermissions("user:wallet:query")
    public R<Map<String, Object>> getWalletStatistics(
            @Parameter(description = "统计开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "统计结束时间")
            @RequestParam(required = false) String endDate) {
        return R.ok(userWalletService.getWalletStatistics(startDate, endDate));
    }

    /**
     * 冻结钱包
     */
    @Operation(summary = "冻结钱包", description = "管理员冻结用户钱包")
    @PutMapping("/{userId}/freeze")
    @RequiresPermissions("user:wallet:admin")
    @Log(title = "冻结钱包", businessType = BusinessType.UPDATE)
    public R<Void> freezeWallet(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "冻结原因")
            @RequestParam(required = false) String reason) {
        return R.result(userWalletService.freezeWallet(userId, reason), "冻结成功", "冻结失败");
    }

    /**
     * 解冻钱包
     */
    @Operation(summary = "解冻钱包", description = "管理员解冻用户钱包")
    @PutMapping("/{userId}/unfreeze")
    @RequiresPermissions("user:wallet:admin")
    @Log(title = "解冻钱包", businessType = BusinessType.UPDATE)
    public R<Void> unfreezeWallet(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        return R.result(userWalletService.unfreezeWallet(userId), "解冻成功", "解冻失败");
    }
}
