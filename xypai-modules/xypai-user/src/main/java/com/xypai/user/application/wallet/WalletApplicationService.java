package com.xypai.user.application.wallet;

import com.xypai.user.application.wallet.command.CreateWalletCommand;
import com.xypai.user.application.wallet.command.RechargeCommand;
import com.xypai.user.application.wallet.command.TransferCommand;
import com.xypai.user.application.wallet.command.WithdrawCommand;
import com.xypai.user.domain.shared.service.DomainEventPublisher;
import com.xypai.user.domain.user.valueobject.UserId;
import com.xypai.user.domain.wallet.WalletAggregate;
import com.xypai.user.domain.wallet.repository.WalletRepository;
import com.xypai.user.domain.wallet.valueobject.WalletId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 💰 钱包应用服务 - 编排钱包业务流程
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletApplicationService {

    private final WalletRepository walletRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * 🔨 创建钱包
     */
    @Transactional
    public WalletId createWallet(CreateWalletCommand command) {
        log.info("创建钱包开始: {}", command);

        // 检查用户是否已有钱包
        if (walletRepository.existsByUserId(command.userId())) {
            throw new IllegalArgumentException("用户已有钱包: " + command.userId());
        }

        // 创建钱包聚合根
        var walletAggregate = WalletAggregate.createWallet(command.userId());

        // 保存聚合根
        var savedAggregate = walletRepository.save(walletAggregate);

        // 发布领域事件
        eventPublisher.publishAll(savedAggregate.getDomainEvents());
        savedAggregate.clearDomainEvents();

        log.info("钱包创建完成: {}", savedAggregate.getWalletId());
        return savedAggregate.getWalletId();
    }

    /**
     * 💳 充值
     */
    @Transactional
    public void recharge(RechargeCommand command) {
        log.info("钱包充值开始: {}", command);

        // 获取钱包聚合根
        var walletAggregate = walletRepository.findByUserId(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("钱包不存在: " + command.userId()));

        // 执行充值
        walletAggregate.recharge(command.amount(), command.paymentMethod());

        // 保存聚合根
        walletRepository.save(walletAggregate);

        // 发布领域事件
        eventPublisher.publishAll(walletAggregate.getDomainEvents());
        walletAggregate.clearDomainEvents();

        log.info("钱包充值完成: userId={}, amount={}", command.userId(), command.amount());
    }

    /**
     * 💸 转账
     */
    @Transactional
    public void transfer(TransferCommand command) {
        log.info("钱包转账开始: {}", command);

        // 获取转出方钱包聚合根
        var fromWallet = walletRepository.findByUserId(command.fromUserId())
                .orElseThrow(() -> new IllegalArgumentException("转出方钱包不存在: " + command.fromUserId()));

        // 验证转入方钱包存在
        if (!walletRepository.existsByUserId(command.toUserId())) {
            throw new IllegalArgumentException("转入方钱包不存在: " + command.toUserId());
        }

        // 执行转账
        fromWallet.transfer(command.toUserId(), command.amount(), command.memo(), command.paymentPassword());

        // 保存转出方钱包
        walletRepository.save(fromWallet);

        // 获取转入方钱包并增加余额（这里简化处理，实际可能需要更复杂的处理）
        var toWallet = walletRepository.findByUserId(command.toUserId()).get();
        toWallet.receiveTransfer(command.fromUserId(), command.amount());
        walletRepository.save(toWallet);

        // 发布领域事件
        eventPublisher.publishAll(fromWallet.getDomainEvents());
        fromWallet.clearDomainEvents();

        log.info("钱包转账完成: {} -> {}, amount={}", command.fromUserId(), command.toUserId(), command.amount());
    }

    /**
     * 🏧 提现
     */
    @Transactional
    public void withdraw(WithdrawCommand command) {
        log.info("钱包提现开始: {}", command);

        // 获取钱包聚合根
        var walletAggregate = walletRepository.findByUserId(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("钱包不存在: " + command.userId()));

        // 执行提现
        walletAggregate.withdraw(command.amount(), command.bankAccount(), command.paymentPassword());

        // 保存聚合根
        walletRepository.save(walletAggregate);

        // 发布领域事件
        eventPublisher.publishAll(walletAggregate.getDomainEvents());
        walletAggregate.clearDomainEvents();

        log.info("钱包提现完成: userId={}, amount={}", command.userId(), command.amount());
    }

    /**
     * 🔒 设置支付密码
     */
    @Transactional
    public void setPaymentPassword(UserId userId, String newPassword) {
        log.info("设置支付密码: userId={}", userId);

        // 获取钱包聚合根
        var walletAggregate = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("钱包不存在: " + userId));

        // 设置支付密码
        walletAggregate.setPaymentPassword(newPassword);

        // 保存聚合根
        walletRepository.save(walletAggregate);

        log.info("支付密码设置完成: userId={}", userId);
    }
}
