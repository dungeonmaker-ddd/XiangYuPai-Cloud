package com.xypai.user.infrastructure.repository.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypai.user.domain.shared.Money;
import com.xypai.user.domain.user.valueobject.UserId;
import com.xypai.user.domain.wallet.WalletAggregate;
import com.xypai.user.domain.wallet.entity.Transaction;
import com.xypai.user.domain.wallet.entity.WalletSettings;
import com.xypai.user.domain.wallet.enums.TransactionStatus;
import com.xypai.user.domain.wallet.enums.TransactionType;
import com.xypai.user.domain.wallet.enums.WalletStatus;
import com.xypai.user.domain.wallet.repository.WalletRepository;
import com.xypai.user.domain.wallet.valueobject.WalletId;
import com.xypai.user.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 钱包仓储实现
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletMapper walletMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletAggregate> findByUserId(UserId userId) {
        try {
            Long userIdValue = Long.parseLong(userId.value());

            // 查询钱包基本信息
            var walletEntity = walletMapper.findByUserId(userIdValue);
            if (walletEntity == null) {
                return Optional.empty();
            }

            // 查询交易记录
            var transactionEntities = walletMapper.findTransactionsByWalletId(
                    walletEntity.getWalletId(), 100, 0
            );

            var transactions = transactionEntities.stream()
                    .map(this::convertToTransaction)
                    .collect(Collectors.toList());

            // 解析钱包设置
            var settings = parseWalletSettings(walletEntity.getSettings());

            // 重构聚合根
            var walletAggregate = WalletAggregate.reconstruct(
                    WalletId.of(walletEntity.getWalletId()),
                    userId,
                    Money.fromCents(walletEntity.getBalance().multiply(BigDecimal.valueOf(100)).longValue(), walletEntity.getCurrency()),
                    Money.fromCents(walletEntity.getFrozenBalance().multiply(BigDecimal.valueOf(100)).longValue(), walletEntity.getCurrency()),
                    WalletStatus.valueOf(walletEntity.getStatus()),
                    settings,
                    transactions,
                    walletEntity.getCreateTime(),
                    walletEntity.getUpdateTime(),
                    walletEntity.getPaymentPassword(),
                    walletEntity.getLastTransactionTime()
            );

            return Optional.of(walletAggregate);

        } catch (Exception e) {
            log.error("查询钱包聚合根失败: userId={}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletAggregate> findByWalletId(WalletId walletId) {
        try {
            Long walletIdValue = Long.parseLong(walletId.value().replace("wallet_", ""));

            var walletEntity = walletMapper.findByWalletId(walletIdValue);
            if (walletEntity == null) {
                return Optional.empty();
            }

            // 查询交易记录
            var transactionEntities = walletMapper.findTransactionsByWalletId(
                    walletEntity.getWalletId(), 100, 0
            );

            var transactions = transactionEntities.stream()
                    .map(this::convertToTransaction)
                    .collect(Collectors.toList());

            var settings = parseWalletSettings(walletEntity.getSettings());

            var walletAggregate = WalletAggregate.reconstruct(
                    walletId,
                    UserId.of(walletEntity.getUserId().toString()),
                    Money.fromCents(walletEntity.getBalance().multiply(BigDecimal.valueOf(100)).longValue(), walletEntity.getCurrency()),
                    Money.fromCents(walletEntity.getFrozenBalance().multiply(BigDecimal.valueOf(100)).longValue(), walletEntity.getCurrency()),
                    WalletStatus.valueOf(walletEntity.getStatus()),
                    settings,
                    transactions,
                    walletEntity.getCreateTime(),
                    walletEntity.getUpdateTime(),
                    walletEntity.getPaymentPassword(),
                    walletEntity.getLastTransactionTime()
            );

            return Optional.of(walletAggregate);

        } catch (Exception e) {
            log.error("查询钱包聚合根失败: walletId={}", walletId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void save(WalletAggregate aggregate) {
        try {
            Long walletIdValue = Long.parseLong(aggregate.getWalletId().value().replace("wallet_", ""));

            // 更新钱包基本信息
            walletMapper.updateBalance(
                    walletIdValue,
                    BigDecimal.valueOf(aggregate.getBalance().toCents()).divide(BigDecimal.valueOf(100)),
                    BigDecimal.valueOf(aggregate.getFrozenBalance().toCents()).divide(BigDecimal.valueOf(100))
            );

            // 更新钱包状态
            walletMapper.updateStatus(walletIdValue, aggregate.getStatus().name());

            // 更新钱包设置
            String settingsJson = serializeWalletSettings(aggregate.getSettings());
            walletMapper.updateSettings(walletIdValue, settingsJson);

            // 保存新的交易记录
            var newTransactions = aggregate.getTransactions().stream()
                    .filter(transaction -> !isTransactionExists(transaction.transactionId().value()))
                    .collect(Collectors.toList());

            for (var transaction : newTransactions) {
                saveTransaction(walletIdValue, transaction);
            }

            log.info("保存钱包聚合根成功: walletId={}, balance={}",
                    aggregate.getWalletId(), aggregate.getBalance());

        } catch (Exception e) {
            log.error("保存钱包聚合根失败: walletId={}", aggregate.getWalletId(), e);
            throw new RuntimeException("保存钱包聚合根失败", e);
        }
    }

    @Override
    @Transactional
    public void delete(WalletAggregate aggregate) {
        try {
            Long walletIdValue = Long.parseLong(aggregate.getWalletId().value().replace("wallet_", ""));

            // 删除所有交易记录
            var transactions = walletMapper.findTransactionsByWalletId(walletIdValue, Integer.MAX_VALUE, 0);
            for (var transaction : transactions) {
                walletMapper.deleteById(transaction.getId());
            }

            // 删除钱包
            walletMapper.deleteById(walletIdValue);

            log.info("删除钱包聚合根成功: walletId={}", aggregate.getWalletId());

        } catch (Exception e) {
            log.error("删除钱包聚合根失败: walletId={}", aggregate.getWalletId(), e);
            throw new RuntimeException("删除钱包聚合根失败", e);
        }
    }

    /**
     * 转换交易实体
     */
    private Transaction convertToTransaction(WalletMapper.TransactionEntity entity) {
        return new Transaction(
                new Transaction.TransactionId(entity.getTransactionId()),
                WalletId.of(entity.getWalletId()),
                TransactionType.valueOf(entity.getType()),
                Money.fromCents(entity.getAmount().multiply(BigDecimal.valueOf(100)).longValue(), "CNY"),
                entity.getFee() != null ? Money.fromCents(entity.getFee().multiply(BigDecimal.valueOf(100)).longValue(), "CNY") : Money.zeroCny(),
                entity.getFromUserId() != null ? UserId.of(entity.getFromUserId().toString()) : null,
                entity.getToUserId() != null ? UserId.of(entity.getToUserId().toString()) : null,
                entity.getDescription(),
                entity.getMemo(),
                TransactionStatus.valueOf(entity.getStatus()),
                entity.getExternalTransactionId(),
                entity.getCreateTime(),
                entity.getCompleteTime(),
                entity.getFailureReason()
        );
    }

    /**
     * 保存交易记录
     */
    private void saveTransaction(Long walletId, Transaction transaction) {
        walletMapper.insertTransaction(
                transaction.transactionId().value(),
                walletId,
                transaction.fromUserId() != null ? Long.parseLong(transaction.fromUserId().value()) : null,
                transaction.toUserId() != null ? Long.parseLong(transaction.toUserId().value()) : null,
                BigDecimal.valueOf(transaction.amount().toCents()).divide(BigDecimal.valueOf(100)),
                transaction.fee() != null ? BigDecimal.valueOf(transaction.fee().toCents()).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO,
                transaction.type().name(),
                transaction.status().name(),
                transaction.description(),
                transaction.memo(),
                transaction.externalTransactionId()
        );
    }

    /**
     * 检查交易是否已存在
     */
    private boolean isTransactionExists(String transactionId) {
        return walletMapper.findTransactionById(transactionId) != null;
    }

    /**
     * 解析钱包设置
     */
    private WalletSettings parseWalletSettings(String settingsJson) {
        if (settingsJson == null || settingsJson.trim().isEmpty()) {
            return WalletSettings.defaultSettings();
        }

        try {
            // 这里可以解析JSON到WalletSettings，现在返回默认设置
            return WalletSettings.defaultSettings();
        } catch (Exception e) {
            log.warn("解析钱包设置失败，使用默认设置: {}", e.getMessage());
            return WalletSettings.defaultSettings();
        }
    }

    /**
     * 序列化钱包设置
     */
    private String serializeWalletSettings(WalletSettings settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            log.error("序列化钱包设置失败", e);
            return "{}";
        }
    }
}
