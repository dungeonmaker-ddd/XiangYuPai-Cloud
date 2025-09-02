package com.xypai.auth.service.impl;

import com.xypai.auth.domain.dto.LoginDTO;
import com.xypai.auth.domain.dto.SmsLoginDTO;
import com.xypai.auth.domain.vo.LoginResultVO;
import com.xypai.auth.feign.UserServiceFeign;
import com.xypai.auth.feign.dto.AuthUserDTO;
import com.xypai.auth.service.IAuthService;
import com.xypai.auth.utils.JwtUtils;
import com.xypai.common.core.domain.R;
import com.xypai.common.redis.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试类
 *
 * @author xypai
 * @date 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserServiceFeign userServiceFeign;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthUserDTO mockUser;
    private LoginDTO loginDTO;
    private SmsLoginDTO smsLoginDTO;

    @BeforeEach
    void setUp() {
        mockUser = AuthUserDTO.builder()
                .id(1L)
                .username("alice_dev")
                .mobile("13800138001")
                .nickname("Alice·全栈开发")
                .status(1)
                .roles(Set.of("USER"))
                .permissions(Set.of("user:read"))
                .build();

        loginDTO = LoginDTO.builder()
                .username("alice_dev")
                .password("123456")
                .clientType("web")
                .build();

        smsLoginDTO = SmsLoginDTO.builder()
                .mobile("13800138001")
                .smsCode("123456")
                .clientType("web")
                .build();
    }

    @Test
    void testLoginWithPassword() {
        // Given
        when(userServiceFeign.getUserByUsername(anyString())).thenReturn(R.ok(mockUser));
        when(userServiceFeign.validatePassword(any())).thenReturn(R.ok(true));
        when(jwtUtils.generateAccessToken(any(), any(), any())).thenReturn("mock-access-token");
        when(jwtUtils.generateRefreshToken(any(), any(), any())).thenReturn("mock-refresh-token");

        // When
        LoginResultVO result = authService.loginWithPassword(loginDTO);

        // Then
        assertNotNull(result);
        assertEquals("mock-access-token", result.getAccessToken());
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertNotNull(result.getUserInfo());
        assertEquals("alice_dev", result.getUserInfo().getUsername());
    }

    @Test
    void testLoginWithInvalidPassword() {
        // Given
        when(userServiceFeign.getUserByUsername(anyString())).thenReturn(R.ok(mockUser));
        when(userServiceFeign.validatePassword(any())).thenReturn(R.ok(false));

        // When & Then
        assertThrows(Exception.class, () -> authService.loginWithPassword(loginDTO));
    }

    @Test
    void testVerifyToken() {
        // Given
        String accessToken = "valid-token";
        when(redisService.hasKey(anyString())).thenReturn(false);
        when(jwtUtils.validateToken(anyString())).thenReturn(true);
        when(jwtUtils.isAccessToken(anyString())).thenReturn(true);
        when(jwtUtils.getAllClaimsFromToken(anyString())).thenReturn(
                java.util.Map.of(
                        "user_id", 1L,
                        "username", "alice_dev",
                        "roles", Set.of("USER"),
                        "permissions", Set.of("user:read")
                )
        );
        when(jwtUtils.getTokenRemainingTime(anyString())).thenReturn(3600L);

        // When
        var result = authService.verifyToken(accessToken);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("valid"));
        assertEquals(1L, result.get("user_id"));
        assertEquals("alice_dev", result.get("username"));
    }

    @Test
    void testLogout() {
        // Given
        String accessToken = "valid-token";
        when(jwtUtils.validateToken(anyString())).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(anyString())).thenReturn("alice_dev");
        when(jwtUtils.getTokenRemainingTime(anyString())).thenReturn(3600L);

        // When
        boolean result = authService.logout(accessToken);

        // Then
        assertTrue(result);
        verify(redisService).setCacheObject(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testLoginWithSms() {
        // Given
        when(userServiceFeign.getUserByMobile(anyString())).thenReturn(R.ok(mockUser));
        when(redisService.getCacheObject(anyString())).thenReturn("123456");
        when(jwtUtils.generateAccessToken(any(), any(), any())).thenReturn("mock-access-token");
        when(jwtUtils.generateRefreshToken(any(), any(), any())).thenReturn("mock-refresh-token");

        // When
        LoginResultVO result = authService.loginWithSms(smsLoginDTO);

        // Then
        assertNotNull(result);
        assertEquals("mock-access-token", result.getAccessToken());
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertNotNull(result.getUserInfo());
        assertEquals("alice_dev", result.getUserInfo().getUsername());
        verify(redisService).deleteObject(anyString()); // 验证验证码被删除
    }

    @Test
    void testLoginWithInvalidSmsCode() {
        // Given
        when(redisService.getCacheObject(anyString())).thenReturn("654321"); // 错误的验证码

        // When & Then
        assertThrows(Exception.class, () -> authService.loginWithSms(smsLoginDTO));
    }
}
