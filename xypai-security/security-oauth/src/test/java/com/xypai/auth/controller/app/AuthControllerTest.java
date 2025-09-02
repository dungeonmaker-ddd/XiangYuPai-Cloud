package com.xypai.auth.controller.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypai.auth.config.SecurityConfig;
import com.xypai.auth.domain.dto.LoginDTO;
import com.xypai.auth.domain.dto.SmsLoginDTO;
import com.xypai.auth.domain.vo.LoginResultVO;
import com.xypai.auth.service.IAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


/**
 * 认证控制器测试类
 *
 * @author xypai
 * @date 2025-01-01
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin() throws Exception {
        // Given
        LoginDTO loginDTO = LoginDTO.builder()
                .username("alice_dev")
                .password("123456")
                .clientType("web")
                .build();

        LoginResultVO.UserInfo userInfo = LoginResultVO.UserInfo.builder()
                .id(1L)
                .username("alice_dev")
                .nickname("Alice·全栈开发")
                .roles(Set.of("USER"))
                .permissions(Set.of("user:read"))
                .build();

        LoginResultVO mockResult = LoginResultVO.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userInfo(userInfo)
                .build();

        when(authService.loginWithPassword(any(LoginDTO.class))).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.userInfo.username").value("alice_dev"));
    }

    @Test
    void testLoginWithInvalidInput() throws Exception {
        // Given - 无效的登录数据
        LoginDTO invalidLoginDTO = LoginDTO.builder()
                .username("") // 空用户名
                .password("123456")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSmsLogin() throws Exception {
        // Given
        SmsLoginDTO smsLoginDTO = SmsLoginDTO.builder()
                .mobile("13800138001")
                .smsCode("123456")
                .clientType("web")
                .build();

        LoginResultVO.UserInfo userInfo = LoginResultVO.UserInfo.builder()
                .id(1L)
                .username("alice_dev")
                .nickname("Alice·全栈开发")
                .roles(Set.of("USER"))
                .permissions(Set.of("user:read"))
                .build();

        LoginResultVO mockResult = LoginResultVO.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userInfo(userInfo)
                .build();

        when(authService.loginWithSms(any(SmsLoginDTO.class))).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/sms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsLoginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.userInfo.username").value("alice_dev"));
    }
}
