package com.xypai.user.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.entity.UserProfile;
import com.xypai.user.dto.UserCreateRequest;
import com.xypai.user.dto.UserResponse;
import com.xypai.user.mapper.UserMapper;
import com.xypai.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 👤 用户服务 - MVP版本
 * <p>
 * 设计原则：
 * - 简单直接
 * - 功能聚焦
 * - 易于测试
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final UserProfileMapper userProfileMapper;

    /**
     * 🔨 创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponse createUser(UserCreateRequest request) {
        log.info("创建用户: {}", request);

        // 1. 验证手机号和用户名唯一性
        validateUserUniqueness(request.mobile(), request.username());

        // 2. 创建用户实体
        User user = new User()
                .setMobile(request.mobile())
                .setUsername(request.username())
                .setNickname(StrUtil.isNotBlank(request.nickname()) ? request.nickname() : request.username())
                .setGender(0)
                .setStatus(1);

        // 3. 保存用户
        save(user);

        // 4. 创建用户扩展信息
        UserProfile profile = new UserProfile()
                .setUserId(user.getId())
                .setPrivacyLevel(1);
        userProfileMapper.insert(profile);

        log.info("用户创建成功: id={}, username={}", user.getId(), user.getUsername());
        return UserResponse.from(user, profile);
    }

    /**
     * 🔍 根据ID获取用户
     */
    public UserResponse getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + id);
        }

        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, id)
        );

        return UserResponse.from(user, profile);
    }

    /**
     * 🔍 根据用户名获取用户
     */
    public UserResponse getUserByUsername(String username) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }

        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, user.getId())
        );

        return UserResponse.from(user, profile);
    }

    /**
     * 📋 获取用户列表
     */
    public List<UserResponse> getUserList() {
        List<User> users = list(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));

        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 🔍 检查手机号是否存在
     */
    public boolean existsByMobile(String mobile) {
        return count(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile)) > 0;
    }

    /**
     * 🔍 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return count(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    /**
     * ✅ 启用用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + id);
        }

        user.setStatus(1);
        updateById(user);
        log.info("用户已启用: id={}", id);
    }

    /**
     * 🚫 禁用用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + id);
        }

        user.setStatus(2);
        updateById(user);
        log.info("用户已禁用: id={}", id);
    }

    // ========================================
    // 私有方法
    // ========================================

    private void validateUserUniqueness(String mobile, String username) {
        if (existsByMobile(mobile)) {
            throw new RuntimeException("手机号已存在: " + mobile);
        }

        if (existsByUsername(username)) {
            throw new RuntimeException("用户名已存在: " + username);
        }
    }
}
