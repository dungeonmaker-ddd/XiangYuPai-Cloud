package com.xypai.user.domain.wallet.repository;

import com.xypai.user.domain.shared.Money;
import com.xypai.user.domain.user.valueobject.UserId;
import com.xypai.user.domain.wallet.WalletAggregate;
import com.xypai.user.domain.wallet.valueobject.WalletId;

import java.util.List;
import java.util.Optional;

/**
 * 💰 钱包仓储接口 - 钱包聚合根持久化
 *
 * @author XyPai
 * @since 2025-01-02
 */
public interface WalletRepository {

    /**
     * 💾 保存钱包聚合根
     */
    WalletAggregate save(WalletAggregate walletAggregate);

    /**
     * 🔍 根据钱包ID查找
     */
    Optional<WalletAggregate> findById(WalletId walletId);

    /**
     * 🔍 根据用户ID查找钱包
     */
    Optional<WalletAggregate> findByUserId(UserId userId);

    /**
     * 🔍 查找所有活跃钱包
     */
    List<WalletAggregate> findActiveWallets();

    /**
     * 🗑️ 删除钱包
     */
    void deleteById(WalletId walletId);

    /**
     * ✅ 检查钱包是否存在
     */
    boolean existsById(WalletId walletId);

    /**
     * ✅ 检查用户是否已有钱包
     */
    boolean existsByUserId(UserId userId);

    /**
     * 💰 获取用户余额
     */
    Money getBalance(UserId userId);

    /**
     * 🔒 冻结余额
     */
    void freezeBalance(UserId userId, Money amount);

    /**
     * 🔓 解冻余额
     */
    void unfreezeBalance(UserId userId, Money amount);
}
