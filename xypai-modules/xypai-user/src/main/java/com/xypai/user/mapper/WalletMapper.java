package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.Transaction;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 钱包Mapper
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Mapper
public interface WalletMapper extends BaseMapper<Transaction> {

    /**
     * 根据用户ID查询钱包信息
     */
    @Select("SELECT * FROM wallet WHERE user_id = #{userId}")
    WalletEntity findByUserId(@Param("userId") Long userId);

    /**
     * 根据钱包ID查询钱包信息
     */
    @Select("SELECT * FROM wallet WHERE wallet_id = #{walletId}")
    WalletEntity findByWalletId(@Param("walletId") Long walletId);

    /**
     * 更新钱包余额
     */
    @Update("""
                UPDATE wallet 
                SET balance = #{balance}, 
                    frozen_balance = #{frozenBalance}, 
                    update_time = NOW(),
                    last_transaction_time = NOW()
                WHERE wallet_id = #{walletId}
            """)
    int updateBalance(@Param("walletId") Long walletId,
                      @Param("balance") BigDecimal balance,
                      @Param("frozenBalance") BigDecimal frozenBalance);

    /**
     * 更新钱包状态
     */
    @Update("UPDATE wallet SET status = #{status}, update_time = NOW() WHERE wallet_id = #{walletId}")
    int updateStatus(@Param("walletId") Long walletId, @Param("status") String status);

    /**
     * 设置支付密码
     */
    @Update("UPDATE wallet SET payment_password = #{password}, update_time = NOW() WHERE wallet_id = #{walletId}")
    int updatePaymentPassword(@Param("walletId") Long walletId, @Param("password") String password);

    /**
     * 更新钱包设置
     */
    @Update("UPDATE wallet SET settings = #{settings}, update_time = NOW() WHERE wallet_id = #{walletId}")
    int updateSettings(@Param("walletId") Long walletId, @Param("settings") String settings);

    /**
     * 插入交易记录
     */
    @Insert("""
                INSERT INTO wallet_transaction 
                (transaction_id, wallet_id, from_user_id, to_user_id, amount, fee, type, status, 
                 description, memo, external_transaction_id, create_time) 
                VALUES 
                (#{transactionId}, #{walletId}, #{fromUserId}, #{toUserId}, #{amount}, #{fee}, 
                 #{type}, #{status}, #{description}, #{memo}, #{externalTransactionId}, NOW())
            """)
    int insertTransaction(@Param("transactionId") String transactionId,
                          @Param("walletId") Long walletId,
                          @Param("fromUserId") Long fromUserId,
                          @Param("toUserId") Long toUserId,
                          @Param("amount") BigDecimal amount,
                          @Param("fee") BigDecimal fee,
                          @Param("type") String type,
                          @Param("status") String status,
                          @Param("description") String description,
                          @Param("memo") String memo,
                          @Param("externalTransactionId") String externalTransactionId);

    /**
     * 根据钱包ID查询交易记录
     */
    @Select("""
                SELECT * FROM wallet_transaction 
                WHERE wallet_id = #{walletId} 
                ORDER BY create_time DESC 
                LIMIT #{limit} OFFSET #{offset}
            """)
    List<TransactionEntity> findTransactionsByWalletId(@Param("walletId") Long walletId,
                                                       @Param("limit") int limit,
                                                       @Param("offset") int offset);

    /**
     * 根据交易ID查询交易记录
     */
    @Select("SELECT * FROM wallet_transaction WHERE transaction_id = #{transactionId}")
    TransactionEntity findTransactionById(@Param("transactionId") String transactionId);

    /**
     * 更新交易状态
     */
    @Update("""
                UPDATE wallet_transaction 
                SET status = #{status}, 
                    complete_time = #{completeTime}, 
                    failure_reason = #{failureReason}
                WHERE transaction_id = #{transactionId}
            """)
    int updateTransactionStatus(@Param("transactionId") String transactionId,
                                @Param("status") String status,
                                @Param("completeTime") LocalDateTime completeTime,
                                @Param("failureReason") String failureReason);

    /**
     * 查询用户指定日期的交易总额
     */
    @Select("""
                SELECT COALESCE(SUM(amount), 0) FROM wallet_transaction wt
                INNER JOIN wallet w ON wt.wallet_id = w.wallet_id
                WHERE w.user_id = #{userId} 
                  AND wt.type = #{transactionType}
                  AND wt.status = 'SUCCESS'
                  AND DATE(wt.create_time) = #{date}
            """)
    BigDecimal getTodayTransactionAmount(@Param("userId") Long userId,
                                         @Param("transactionType") String transactionType,
                                         @Param("date") LocalDate date);

    /**
     * 查询用户的交易统计
     */
    @Select("""
                SELECT 
                    COUNT(1) as total_count,
                    COALESCE(SUM(CASE WHEN type IN ('RECHARGE', 'TRANSFER_IN', 'REFUND', 'REWARD') THEN amount ELSE 0 END), 0) as total_income,
                    COALESCE(SUM(CASE WHEN type IN ('WITHDRAW', 'TRANSFER_OUT', 'PAYMENT') THEN amount ELSE 0 END), 0) as total_expense
                FROM wallet_transaction wt
                INNER JOIN wallet w ON wt.wallet_id = w.wallet_id
                WHERE w.user_id = #{userId} AND wt.status = 'SUCCESS'
            """)
    TransactionStatsEntity getTransactionStats(@Param("userId") Long userId);

    /**
     * 查询待处理的交易记录
     */
    @Select("""
                SELECT * FROM wallet_transaction 
                WHERE status = 'PENDING' 
                  AND create_time <= DATE_SUB(NOW(), INTERVAL #{timeoutMinutes} MINUTE)
                ORDER BY create_time ASC
            """)
    List<TransactionEntity> findTimeoutTransactions(@Param("timeoutMinutes") int timeoutMinutes);

    /**
     * 根据外部交易ID查询交易记录
     */
    @Select("SELECT * FROM wallet_transaction WHERE external_transaction_id = #{externalTransactionId}")
    TransactionEntity findByExternalTransactionId(@Param("externalTransactionId") String externalTransactionId);

    /**
     * 统计钱包余额分布
     */
    @Select("""
                SELECT 
                    COUNT(CASE WHEN balance = 0 THEN 1 END) as zero_balance_count,
                    COUNT(CASE WHEN balance > 0 AND balance <= 100 THEN 1 END) as small_balance_count,
                    COUNT(CASE WHEN balance > 100 AND balance <= 1000 THEN 1 END) as medium_balance_count,
                    COUNT(CASE WHEN balance > 1000 THEN 1 END) as large_balance_count,
                    AVG(balance) as average_balance,
                    SUM(balance) as total_balance
                FROM wallet WHERE status = 'ACTIVE'
            """)
    WalletStatsEntity getWalletStats();

    /**
     * 钱包实体类（用于映射数据库字段）
     */
    public static class WalletEntity {
        private Long walletId;
        private Long userId;
        private BigDecimal balance;
        private BigDecimal frozenBalance;
        private String currency;
        private String status;
        private String paymentPassword;
        private String settings;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private LocalDateTime lastTransactionTime;

        // Getters and Setters
        public Long getWalletId() {
            return walletId;
        }

        public void setWalletId(Long walletId) {
            this.walletId = walletId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public BigDecimal getFrozenBalance() {
            return frozenBalance;
        }

        public void setFrozenBalance(BigDecimal frozenBalance) {
            this.frozenBalance = frozenBalance;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPaymentPassword() {
            return paymentPassword;
        }

        public void setPaymentPassword(String paymentPassword) {
            this.paymentPassword = paymentPassword;
        }

        public String getSettings() {
            return settings;
        }

        public void setSettings(String settings) {
            this.settings = settings;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }

        public LocalDateTime getLastTransactionTime() {
            return lastTransactionTime;
        }

        public void setLastTransactionTime(LocalDateTime lastTransactionTime) {
            this.lastTransactionTime = lastTransactionTime;
        }
    }

    /**
     * 交易实体类（用于映射数据库字段）
     */
    public static class TransactionEntity {
        private Long id;
        private String transactionId;
        private Long walletId;
        private Long fromUserId;
        private Long toUserId;
        private BigDecimal amount;
        private BigDecimal fee;
        private String type;
        private String status;
        private String description;
        private String memo;
        private String externalTransactionId;
        private LocalDateTime createTime;
        private LocalDateTime completeTime;
        private String failureReason;

        // Getters and Setters (省略，实际项目中需要添加)
    }

    /**
     * 交易统计实体类
     */
    public static class TransactionStatsEntity {
        private Integer totalCount;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;

        // Getters and Setters (省略，实际项目中需要添加)
    }

    /**
     * 钱包统计实体类
     */
    public static class WalletStatsEntity {
        private Integer zeroBalanceCount;
        private Integer smallBalanceCount;
        private Integer mediumBalanceCount;
        private Integer largeBalanceCount;
        private BigDecimal averageBalance;
        private BigDecimal totalBalance;

        // Getters and Setters (省略，实际项目中需要添加)
    }
}
