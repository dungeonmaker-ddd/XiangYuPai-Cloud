package com.xypai.user.service.impl;

import com.xypai.common.core.exception.ServiceException;
import com.xypai.user.domain.dto.TransactionQueryDTO;
import com.xypai.user.domain.dto.WalletRechargeDTO;
import com.xypai.user.domain.dto.WalletTransferDTO;
import com.xypai.user.domain.entity.Transaction;
import com.xypai.user.domain.entity.UserWallet;
import com.xypai.user.domain.vo.TransactionVO;
import com.xypai.user.domain.vo.UserWalletVO;
import com.xypai.user.mapper.TransactionMapper;
import com.xypai.user.mapper.UserWalletMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.xypai.common.security.utils.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户钱包服务测试类
 *
 * @author xypai
 * @date 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class UserWalletServiceImplTest {

    @Mock
    private UserWalletMapper userWalletMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private UserWalletServiceImpl walletService;

    private UserWallet testWallet;
    private Transaction testTransaction;
    private WalletRechargeDTO testRechargeDTO;
    private WalletTransferDTO testTransferDTO;

    @BeforeEach
    void setUp() {
        testWallet = UserWallet.builder()
                .userId(1L)
                .balance(10000L) // 100元
                .version(0)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .userId(1L)
                .amount(5000L) // 50元
                .type(Transaction.Type.RECHARGE.getCode())
                .refId("RO1234567890")
                .createdAt(LocalDateTime.now())
                .build();

        testRechargeDTO = WalletRechargeDTO.builder()
                .amount(new BigDecimal("50.00"))
                .paymentMethod("alipay")
                .description("测试充值")
                .build();

        testTransferDTO = WalletTransferDTO.builder()
                .toUserId(2L)
                .amount(new BigDecimal("20.00"))
                .description("测试转账")
                .build();
    }

    @Test
    void testGetUserWalletByUserId_Success() {
        // Given
        when(userWalletMapper.selectById(1L)).thenReturn(testWallet);

        // When
        UserWalletVO result = walletService.getUserWalletByUserId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("¥100.00", result.getBalance());
        assertEquals(10000L, result.getBalanceFen());
        assertTrue(result.getAvailable());
        verify(userWalletMapper).selectById(1L);
    }

    @Test
    void testGetUserWalletByUserId_WalletNotFound() {
        // Given
        when(userWalletMapper.selectById(1L)).thenReturn(null);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> walletService.getUserWalletByUserId(1L));
        assertEquals("用户钱包不存在", exception.getMessage());
    }

    @Test
    void testGetUserWallet_Success() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userWalletMapper.selectById(1L)).thenReturn(testWallet);

            // When
            UserWalletVO result = walletService.getUserWallet();

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            assertEquals("¥100.00", result.getBalance());
        }
    }

    @Test
    void testCreateRechargeOrder_Success() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            // When
            String orderNo = walletService.createRechargeOrder(testRechargeDTO);

            // Then
            assertNotNull(orderNo);
            assertTrue(orderNo.startsWith("RO"));
        }
    }

    @Test
    void testTransferMoney_Success() {
        // Given
        UserWallet fromWallet = UserWallet.builder()
                .userId(1L)
                .balance(10000L) // 100元
                .version(0)
                .build();
        
        UserWallet toWallet = UserWallet.builder()
                .userId(2L)
                .balance(5000L) // 50元
                .version(0)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userWalletMapper.selectById(1L)).thenReturn(fromWallet);
            when(userWalletMapper.selectById(2L)).thenReturn(toWallet);
            when(userWalletMapper.updateById(any(UserWallet.class))).thenReturn(1);
            when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

            // When
            boolean result = walletService.transferMoney(testTransferDTO);

            // Then
            assertTrue(result);
            verify(userWalletMapper, times(2)).updateById(any(UserWallet.class));
            verify(transactionMapper, times(2)).insert(any(Transaction.class));
        }
    }

    @Test
    void testTransferMoney_InsufficientBalance() {
        // Given
        UserWallet fromWallet = UserWallet.builder()
                .userId(1L)
                .balance(1000L) // 10元，不足以转账20元
                .version(0)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userWalletMapper.selectById(1L)).thenReturn(fromWallet);

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class, 
                () -> walletService.transferMoney(testTransferDTO));
            assertEquals("余额不足", exception.getMessage());
        }
    }

    @Test
    void testTransferMoney_SelfTransfer() {
        // Given
        testTransferDTO.setToUserId(1L); // 转账给自己

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class, 
                () -> walletService.transferMoney(testTransferDTO));
            assertEquals("不能向自己转账", exception.getMessage());
        }
    }

    @Test
    void testGetUserTransactions_Success() {
        // Given
        TransactionQueryDTO query = TransactionQueryDTO.builder()
                .type(Transaction.Type.RECHARGE.getCode())
                .build();

        List<Transaction> mockTransactions = Arrays.asList(testTransaction);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(transactionMapper.selectList(any())).thenReturn(mockTransactions);

            // When
            List<TransactionVO> result = walletService.getUserTransactions(query);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals("50.00", result.get(0).getAmount());
            assertTrue(result.get(0).getIsIncome());
        }
    }

    @Test
    void testGetTransactionById_Success() {
        // Given
        when(transactionMapper.selectById(1L)).thenReturn(testTransaction);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            // When
            TransactionVO result = walletService.getTransactionById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("50.00", result.getAmount());
            assertEquals("充值", result.getTypeDesc());
        }
    }

    @Test
    void testGetTransactionById_NotFound() {
        // Given
        when(transactionMapper.selectById(1L)).thenReturn(null);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> walletService.getTransactionById(1L));
        assertEquals("交易记录不存在", exception.getMessage());
    }

    @Test
    void testGetWalletStatistics_Success() {
        // Given
        List<Transaction> mockTransactions = Arrays.asList(
            Transaction.builder()
                .userId(1L)
                .amount(5000L)
                .type(Transaction.Type.RECHARGE.getCode())
                .build(),
            Transaction.builder()
                .userId(1L)
                .amount(-2000L)
                .type(Transaction.Type.CONSUME.getCode())
                .build()
        );

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userWalletMapper.selectById(1L)).thenReturn(testWallet);
            when(transactionMapper.selectList(any())).thenReturn(mockTransactions);

            // When
            Map<String, Object> result = walletService.getWalletStatistics(null, null);

            // Then
            assertNotNull(result);
            assertEquals("¥100.00", result.get("currentBalance"));
            assertEquals("¥50.00", result.get("totalIncome"));
            assertEquals("¥20.00", result.get("totalExpense"));
            assertEquals(2, result.get("transactionCount"));
        }
    }

    @Test
    void testCheckBalance_Success() {
        // Given
        when(userWalletMapper.selectById(1L)).thenReturn(testWallet);

        // When
        boolean result = walletService.checkBalance(1L, 5000L);

        // Then
        assertTrue(result);
    }

    @Test
    void testCheckBalance_InsufficientBalance() {
        // Given
        when(userWalletMapper.selectById(1L)).thenReturn(testWallet);

        // When
        boolean result = walletService.checkBalance(1L, 15000L);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdateBalance_Success() {
        // Given
        when(userWalletMapper.selectById(1L)).thenReturn(testWallet);
        when(userWalletMapper.updateById(any(UserWallet.class))).thenReturn(1);
        when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

        // When
        boolean result = walletService.updateBalance(1L, 5000L, 
            Transaction.Type.RECHARGE.getCode(), "test123");

        // Then
        assertTrue(result);
        verify(userWalletMapper).updateById(any(UserWallet.class));
        verify(transactionMapper).insert(any(Transaction.class));
    }

    @Test
    void testUpdateBalance_InsufficientBalance() {
        // Given
        when(userWalletMapper.selectById(1L)).thenReturn(testWallet);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> walletService.updateBalance(1L, -15000L, 
                Transaction.Type.CONSUME.getCode(), "test123"));
        assertEquals("余额不足", exception.getMessage());
    }

    @Test
    void testConsumeBalance_Success() {
        // Given
        when(userWalletMapper.selectById(1L)).thenReturn(testWallet);
        when(userWalletMapper.updateById(any(UserWallet.class))).thenReturn(1);
        when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

        // When
        boolean result = walletService.consumeBalance(1L, 3000L, 
            Transaction.Type.CONSUME.getCode(), "order123");

        // Then
        assertTrue(result);
        verify(userWalletMapper).updateById(any(UserWallet.class));
        verify(transactionMapper).insert(any(Transaction.class));
    }

    @Test
    void testRefundBalance_Success() {
        // Given
        when(userWalletMapper.selectById(1L)).thenReturn(testWallet);
        when(userWalletMapper.updateById(any(UserWallet.class))).thenReturn(1);
        when(transactionMapper.insert(any(Transaction.class))).thenReturn(1);

        // When
        boolean result = walletService.refundBalance(1L, 2000L, 
            Transaction.Type.REFUND.getCode(), "refund123");

        // Then
        assertTrue(result);
        verify(userWalletMapper).updateById(any(UserWallet.class));
        verify(transactionMapper).insert(any(Transaction.class));
    }
}
