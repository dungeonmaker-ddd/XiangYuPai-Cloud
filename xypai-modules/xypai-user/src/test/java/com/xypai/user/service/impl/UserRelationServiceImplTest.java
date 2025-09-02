package com.xypai.user.service.impl;

import com.xypai.common.core.exception.ServiceException;
import com.xypai.user.domain.dto.UserRelationQueryDTO;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.entity.UserProfile;
import com.xypai.user.domain.entity.UserRelation;
import com.xypai.user.domain.vo.UserRelationVO;
import com.xypai.user.mapper.UserMapper;
import com.xypai.user.mapper.UserProfileMapper;
import com.xypai.user.mapper.UserRelationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.xypai.common.security.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户关系服务测试类
 *
 * @author xypai
 * @date 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class UserRelationServiceImplTest {

    @Mock
    private UserRelationMapper userRelationMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserRelationServiceImpl relationService;

    private User testUser;
    private User targetUser;
    private UserProfile testProfile;
    private UserProfile targetProfile;
    private UserRelation testRelation;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .mobile("13800138001")
                .status(1)
                .createdAt(LocalDateTime.now())
                .build();

        targetUser = User.builder()
                .id(2L)
                .username("targetuser")
                .mobile("13800138002")
                .status(1)
                .createdAt(LocalDateTime.now())
                .build();

        testProfile = UserProfile.builder()
                .userId(1L)
                .nickname("测试用户")
                .avatar("http://example.com/avatar1.jpg")
                .build();

        targetProfile = UserProfile.builder()
                .userId(2L)
                .nickname("目标用户")
                .avatar("http://example.com/avatar2.jpg")
                .build();

        testRelation = UserRelation.builder()
                .id(1L)
                .userId(1L)
                .targetId(2L)
                .type(UserRelation.Type.FOLLOW.getCode())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testFollowUser_Success() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userMapper.selectById(2L)).thenReturn(targetUser);
            when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.FOLLOW.getCode())).thenReturn(null);
            when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.BLOCK.getCode())).thenReturn(null);
            when(userRelationMapper.selectRelation(2L, 1L, UserRelation.Type.BLOCK.getCode())).thenReturn(null);
            when(userRelationMapper.insert(any(UserRelation.class))).thenReturn(1);

            // When
            boolean result = relationService.followUser(2L);

            // Then
            assertTrue(result);
            verify(userRelationMapper).insert(any(UserRelation.class));
        }
    }

    @Test
    void testFollowUser_SelfFollow() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class, 
                () -> relationService.followUser(1L));
            assertEquals("不能关注自己", exception.getMessage());
        }
    }

    @Test
    void testFollowUser_UserNotFound() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userMapper.selectById(2L)).thenReturn(null);

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class, 
                () -> relationService.followUser(2L));
            assertEquals("用户不存在", exception.getMessage());
        }
    }

    @Test
    void testFollowUser_AlreadyFollowing() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userMapper.selectById(2L)).thenReturn(targetUser);
            when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.FOLLOW.getCode())).thenReturn(testRelation);

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class, 
                () -> relationService.followUser(2L));
            assertEquals("已经关注了该用户", exception.getMessage());
        }
    }

    @Test
    void testUnfollowUser_Success() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.delete(any())).thenReturn(1);

            // When
            boolean result = relationService.unfollowUser(2L);

            // Then
            assertTrue(result);
            verify(userRelationMapper).delete(any());
        }
    }

    @Test
    void testBlockUser_Success() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userMapper.selectById(2L)).thenReturn(targetUser);
            when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.FOLLOW.getCode())).thenReturn(null);
            when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.BLOCK.getCode())).thenReturn(null);
            when(userRelationMapper.insert(any(UserRelation.class))).thenReturn(1);

            // When
            boolean result = relationService.blockUser(2L);

            // Then
            assertTrue(result);
            verify(userRelationMapper).insert(any(UserRelation.class));
        }
    }

    @Test
    void testBlockUser_SelfBlock() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class, 
                () -> relationService.blockUser(1L));
            assertEquals("不能拉黑自己", exception.getMessage());
        }
    }

    @Test
    void testUnblockUser_Success() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.delete(any())).thenReturn(1);

            // When
            boolean result = relationService.unblockUser(2L);

            // Then
            assertTrue(result);
            verify(userRelationMapper).delete(any());
        }
    }

    @Test
    void testGetFollowingList_Success() {
        // Given
        UserRelationQueryDTO query = UserRelationQueryDTO.builder().build();
        List<UserRelation> mockRelations = Arrays.asList(testRelation);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.selectList(any())).thenReturn(mockRelations);
            when(userMapper.selectById(2L)).thenReturn(targetUser);
            when(userProfileMapper.selectById(2L)).thenReturn(targetProfile);

            // When
            List<UserRelationVO> result = relationService.getFollowingList(query);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(2L, result.get(0).getTargetUserId());
            assertEquals("目标用户", result.get(0).getTargetUser().getNickname());
            assertTrue(result.get(0).getIsFollow());
        }
    }

    @Test
    void testGetFollowersList_Success() {
        // Given
        UserRelationQueryDTO query = UserRelationQueryDTO.builder().build();
        UserRelation followerRelation = UserRelation.builder()
                .id(2L)
                .userId(2L)
                .targetId(1L)
                .type(UserRelation.Type.FOLLOW.getCode())
                .createdAt(LocalDateTime.now())
                .build();
        List<UserRelation> mockRelations = Arrays.asList(followerRelation);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.selectList(any())).thenReturn(mockRelations);
            when(userMapper.selectById(2L)).thenReturn(targetUser);
            when(userProfileMapper.selectById(2L)).thenReturn(targetProfile);

            // When
            List<UserRelationVO> result = relationService.getFollowersList(query);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getTargetUserId());
        }
    }

    @Test
    void testCheckUserRelation_Success() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.FOLLOW.getCode())).thenReturn(testRelation);
            when(userRelationMapper.selectRelation(2L, 1L, UserRelation.Type.FOLLOW.getCode())).thenReturn(null);
            when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.BLOCK.getCode())).thenReturn(null);

            // When
            Map<String, Boolean> result = relationService.checkUserRelation(2L);

            // Then
            assertNotNull(result);
            assertTrue(result.get("isFollowing"));
            assertFalse(result.get("isFollower"));
            assertFalse(result.get("isBlocked"));
            assertFalse(result.get("isMutual"));
        }
    }

    @Test
    void testGetRelationStatistics_Success() {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.countFollowing(1L)).thenReturn(10L);
            when(userRelationMapper.countFollowers(1L)).thenReturn(5L);
            when(userRelationMapper.selectCount(any())).thenReturn(2L);

            // When
            Map<String, Long> result = relationService.getRelationStatistics();

            // Then
            assertNotNull(result);
            assertEquals(10L, result.get("followingCount"));
            assertEquals(5L, result.get("followersCount"));
            assertEquals(2L, result.get("blockedCount"));
        }
    }

    @Test
    void testBatchFollowUsers_Success() {
        // Given
        List<Long> userIds = Arrays.asList(2L, 3L, 4L);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.selectRelation(anyLong(), anyLong(), anyInt())).thenReturn(null);
            when(userRelationMapper.insert(any(UserRelation.class))).thenReturn(1);

            // When
            boolean result = relationService.batchFollowUsers(userIds);

            // Then
            assertTrue(result);
            verify(userRelationMapper, times(3)).insert(any(UserRelation.class));
        }
    }

    @Test
    void testBatchUnfollowUsers_Success() {
        // Given
        List<Long> userIds = Arrays.asList(2L, 3L, 4L);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.delete(any())).thenReturn(3);

            // When
            boolean result = relationService.batchUnfollowUsers(userIds);

            // Then
            assertTrue(result);
            verify(userRelationMapper).delete(any());
        }
    }

    @Test
    void testIsFollowing_True() {
        // Given
        when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.FOLLOW.getCode())).thenReturn(testRelation);

        // When
        boolean result = relationService.isFollowing(1L, 2L);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsFollowing_False() {
        // Given
        when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.FOLLOW.getCode())).thenReturn(null);

        // When
        boolean result = relationService.isFollowing(1L, 2L);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsBlocked_True() {
        // Given
        UserRelation blockRelation = UserRelation.builder()
                .userId(1L)
                .targetId(2L)
                .type(UserRelation.Type.BLOCK.getCode())
                .build();
        when(userRelationMapper.selectRelation(1L, 2L, UserRelation.Type.BLOCK.getCode())).thenReturn(blockRelation);

        // When
        boolean result = relationService.isBlocked(1L, 2L);

        // Then
        assertTrue(result);
    }

    @Test
    void testGetMutualFollowingList_Success() {
        // Given
        List<UserRelation> myFollowing = Arrays.asList(testRelation);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.selectList(any())).thenReturn(myFollowing);
            when(userRelationMapper.selectRelation(2L, 1L, UserRelation.Type.FOLLOW.getCode())).thenReturn(testRelation);
            when(userMapper.selectById(2L)).thenReturn(targetUser);
            when(userProfileMapper.selectById(2L)).thenReturn(targetProfile);

            // When
            List<UserRelationVO> result = relationService.getMutualFollowingList();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(2L, result.get(0).getTargetUserId());
        }
    }

    @Test
    void testGetRecommendedUsers_Success() {
        // Given
        List<User> recommendedUsers = Arrays.asList(targetUser);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            when(userRelationMapper.selectList(any())).thenReturn(Arrays.asList());
            when(userMapper.selectList(any())).thenReturn(recommendedUsers);
            when(userProfileMapper.selectById(2L)).thenReturn(targetProfile);

            // When
            List<UserRelationVO> result = relationService.getRecommendedUsers(10);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(2L, result.get(0).getTargetUserId());
            assertEquals("目标用户", result.get(0).getTargetUser().getNickname());
        }
    }
}
