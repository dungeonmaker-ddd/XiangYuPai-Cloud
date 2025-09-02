package com.xypai.user.controller.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypai.user.domain.dto.UserAddDTO;
import com.xypai.user.domain.dto.UserUpdateDTO;
import com.xypai.user.domain.vo.UserDetailVO;
import com.xypai.user.domain.vo.UserListVO;
import com.xypai.user.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器测试类
 *
 * @author xypai
 * @date 2025-01-01
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetUserInfo_Success() throws Exception {
        // Given
        UserDetailVO userVO = UserDetailVO.builder()
                .id(1L)
                .username("testuser")
                .nickname("测试用户")
                .mobile("13800138001")
                .status(1)
                .statusDesc("正常")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.selectUserById(1L)).thenReturn(userVO);

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));
    }

    @Test
    void testAddUser_Success() throws Exception {
        // Given
        UserAddDTO userAddDTO = UserAddDTO.builder()
                .username("newuser")
                .mobile("13800138999")
                .password("123456")
                .nickname("新用户")
                .email("test@example.com")
                .build();

        when(userService.insertUser(any(UserAddDTO.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAddDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testAddUser_ValidationError() throws Exception {
        // Given
        UserAddDTO userAddDTO = UserAddDTO.builder()
                .username("") // 空用户名，应该验证失败
                .mobile("invalid_mobile") // 无效手机号
                .password("123") // 密码太短
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAddDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Given
        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .id(1L)
                .username("updateduser")
                .nickname("更新的用户")
                .build();

        when(userService.updateUser(any(UserUpdateDTO.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Given
        when(userService.deleteUserByIds(anyList())).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetUserList_Success() throws Exception {
        // Given
        List<UserListVO> userList = Arrays.asList(
            UserListVO.builder()
                .id(1L)
                .username("user1")
                .nickname("用户1")
                .build(),
            UserListVO.builder()
                .id(2L)
                .username("user2")
                .nickname("用户2")
                .build()
        );

        when(userService.selectUserList(any())).thenReturn(userList);

        // When & Then
        mockMvc.perform(get("/api/v1/users/list")
                .param("username", "test")
                .param("status", "1"))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows.length()").value(2))
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    void testResetPassword_Success() throws Exception {
        // Given
        when(userService.resetUserPassword(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/v1/users/1/reset-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testChangeStatus_Success() throws Exception {
        // Given
        when(userService.updateUserStatus(1L, 2)).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/v1/users/1/status")
                .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCheckUsername_Unique() throws Exception {
        // Given
        when(userService.checkUsernameUnique("newuser", null)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/users/check-username")
                .param("username", "newuser"))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testCheckMobile_NotUnique() throws Exception {
        // Given
        when(userService.checkMobileUnique("13800138001", null)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/users/check-mobile")
                .param("mobile", "13800138001"))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }
}
