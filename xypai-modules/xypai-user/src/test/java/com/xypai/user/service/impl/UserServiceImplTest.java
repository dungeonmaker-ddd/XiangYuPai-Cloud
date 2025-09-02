package com.xypai.user.service.impl;

import com.xypai.common.core.exception.ServiceException;
import com.xypai.user.domain.dto.UserAddDTO;
import com.xypai.user.domain.dto.UserQueryDTO;
import com.xypai.user.domain.dto.UserUpdateDTO;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.entity.UserProfile;
import com.xypai.user.domain.entity.UserWallet;
import com.xypai.user.domain.vo.UserDetailVO;
import com.xypai.user.domain.vo.UserListVO;
import com.xypai.user.mapper.UserMapper;
import com.xypai.user.mapper.UserProfileMapper;
import com.xypai.user.mapper.UserWalletMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 *
 * @author xypai
 * @date 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private UserWalletMapper userWalletMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserProfile testProfile;
    private UserWallet testWallet;
    private UserAddDTO testAddDTO;
    private UserUpdateDTO testUpdateDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .mobile("13800138001")
                .password("encoded_password")
                .status(1)
                .createdAt(LocalDateTime.now())
                .version(0)
                .build();

        testProfile = UserProfile.builder()
                .userId(1L)
                .nickname("测试用户")
                .avatar("http://example.com/avatar.jpg")
                .version(0)
                .build();

        testWallet = UserWallet.builder()
                .userId(1L)
                .balance(10000L) // 100元
                .version(0)
                .build();

        testAddDTO = UserAddDTO.builder()
                .username("newuser")
                .mobile("13800138002")
                .password("123456")
                .nickname("新用户")
                .email("test@example.com")
                .build();

        testUpdateDTO = UserUpdateDTO.builder()
                .id(1L)
                .username("updateduser")
                .nickname("更新的用户")
                .version(0)
                .build();
    }

    @Test
    void testSelectUserList() {
        // Given
        UserQueryDTO query = UserQueryDTO.builder()
                .username("test")
                .status(1)
                .build();

        List<User> mockUsers = Arrays.asList(testUser);
        when(userMapper.selectList(any())).thenReturn(mockUsers);
        when(userProfileMapper.selectById(1L)).thenReturn(testProfile);

        // When
        List<UserListVO> result = userService.selectUserList(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("测试用户", result.get(0).getNickname());
        verify(userMapper).selectList(any());
    }

    @Test
    void testSelectUserById_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userProfileMapper.selectById(1L)).thenReturn(testProfile);
        when(userWalletMapper.selectById(1L)).thenReturn(testWallet);

        // When
        UserDetailVO result = userService.selectUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getNickname());
        assertEquals("¥100.00", result.getWalletBalance());
        verify(userMapper).selectById(1L);
    }

    @Test
    void testSelectUserById_UserNotFound() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(null);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> userService.selectUserById(1L));
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testInsertUser_Success() {
        // Given
        when(userMapper.selectCount(any())).thenReturn(0L); // 用户名和手机号都不存在
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(userProfileMapper.insert(any(UserProfile.class))).thenReturn(1);
        when(userWalletMapper.insert(any(UserWallet.class))).thenReturn(1);

        // When
        boolean result = userService.insertUser(testAddDTO);

        // Then
        assertTrue(result);
        verify(userMapper).insert(any(User.class));
        verify(userProfileMapper).insert(any(UserProfile.class));
        verify(userWalletMapper).insert(any(UserWallet.class));
    }

    @Test
    void testInsertUser_UsernameExists() {
        // Given
        when(userMapper.selectCount(any())).thenReturn(1L); // 用户名已存在

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> userService.insertUser(testAddDTO));
        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.selectCount(any())).thenReturn(0L); // 用户名和手机号都不冲突
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(userProfileMapper.selectById(1L)).thenReturn(testProfile);
        when(userProfileMapper.updateById(any(UserProfile.class))).thenReturn(1);

        // When
        boolean result = userService.updateUser(testUpdateDTO);

        // Then
        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
        verify(userProfileMapper).updateById(any(UserProfile.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(null);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> userService.updateUser(testUpdateDTO));
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testDeleteUserByIds_Success() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L);
        when(userMapper.deleteBatchIds(userIds)).thenReturn(2);

        // When
        boolean result = userService.deleteUserByIds(userIds);

        // Then
        assertTrue(result);
        verify(userMapper).deleteBatchIds(userIds);
    }

    @Test
    void testCheckUsernameUnique_Unique() {
        // Given
        when(userMapper.selectCount(any())).thenReturn(0L);

        // When
        boolean result = userService.checkUsernameUnique("newuser", null);

        // Then
        assertTrue(result);
    }

    @Test
    void testCheckUsernameUnique_NotUnique() {
        // Given
        when(userMapper.selectCount(any())).thenReturn(1L);

        // When
        boolean result = userService.checkUsernameUnique("existinguser", null);

        // Then
        assertFalse(result);
    }

    @Test
    void testCheckMobileUnique_Unique() {
        // Given
        when(userMapper.selectCount(any())).thenReturn(0L);

        // When
        boolean result = userService.checkMobileUnique("13800138999", null);

        // Then
        assertTrue(result);
    }

    @Test
    void testValidatePassword_Success() {
        // Given
        String rawPassword = "123456";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(rawPassword);
        
        User userWithEncodedPassword = User.builder()
                .id(1L)
                .password(encodedPassword)
                .build();
        
        when(userMapper.selectById(1L)).thenReturn(userWithEncodedPassword);

        // When
        boolean result = userService.validatePassword(1L, rawPassword);

        // Then
        assertTrue(result);
    }

    @Test
    void testValidatePassword_WrongPassword() {
        // Given
        String wrongPassword = "wrongpassword";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("correctpassword");
        
        User userWithEncodedPassword = User.builder()
                .id(1L)
                .password(encodedPassword)
                .build();
        
        when(userMapper.selectById(1L)).thenReturn(userWithEncodedPassword);

        // When
        boolean result = userService.validatePassword(1L, wrongPassword);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdateUserStatus_Success() {
        // Given
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.updateUserStatus(1L, 2);

        // Then
        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void testResetUserPassword_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.resetUserPassword(1L);

        // Then
        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void testActivateUser() {
        // Given
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.activateUser(1L);

        // Then
        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void testFreezeUser() {
        // Given
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.freezeUser(1L, "违规行为");

        // Then
        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
    }
}
