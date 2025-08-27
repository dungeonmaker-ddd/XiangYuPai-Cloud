package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.record.UserCreateRequest;
import com.xypai.user.domain.record.UserQueryRequest;
import com.xypai.user.domain.record.UserResponse;
import com.xypai.user.domain.record.UserUpdateRequest;
import com.xypai.user.mapper.UserMapper;
import com.xypai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 用户服务实现类
 *
 * @author XyPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse createUser(UserCreateRequest request) {
        Objects.requireNonNull(request, "用户创建请求不能为null");

        // 验证用户名、邮箱、手机号唯一性
        validateUserUniqueness(request.username(), request.email(), request.phone(), null);

        // 转换为实体
        User user = convertToEntity(request);

        // 加密密码
        user.setPassword(SecurityUtils.encryptPassword(request.password()));

        // 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("用户创建失败");
        }

        log.info("用户创建成功: {}", user.getUsername());
        return convertToResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<UserResponse> updateUser(UserUpdateRequest request) {
        Objects.requireNonNull(request, "用户更新请求不能为null");

        User user = userMapper.selectById(request.userId());
        if (user == null) {
            return Optional.empty();
        }

        // 验证唯一性
        request.email().ifPresent(email -> {
            if (existsEmail(email, request.userId())) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        });

        request.phone().ifPresent(phone -> {
            if (existsPhone(phone, request.userId())) {
                throw new IllegalArgumentException("手机号已存在");
            }
        });

        // 更新字段
        updateUserFields(user, request);

        int result = userMapper.updateById(user);
        if (result <= 0) {
            return Optional.empty();
        }

        log.info("用户更新成功: {}", user.getUsername());
        return Optional.of(convertToResponse(user));
    }

    @Override
    public Optional<UserResponse> getUserById(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        User user = userMapper.selectById(userId);
        return user != null ? Optional.of(convertToResponse(user)) : Optional.empty();
    }

    @Override
    public Optional<UserResponse> getUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return Optional.empty();
        }

        return userMapper.selectByUsername(username)
                .map(this::convertToResponse);
    }

    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return Optional.empty();
        }

        return userMapper.selectByEmail(email)
                .map(this::convertToResponse);
    }

    @Override
    public Optional<UserResponse> getUserByPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return Optional.empty();
        }

        return userMapper.selectByPhone(phone)
                .map(this::convertToResponse);
    }

    @Override
    public IPage<UserResponse> getUserPage(UserQueryRequest request) {
        Objects.requireNonNull(request, "查询请求不能为null");

        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<User> userPage = userMapper.selectUserPage(page, request);

        return userPage.convert(this::convertToResponse);
    }

    @Override
    public List<UserResponse> getUsersByDeptId(Long deptId) {
        // 部门功能暂时不支持，返回空列表
        return List.of();
    }

    @Override
    public List<UserResponse> getUsersByStatus(Integer status) {
        Objects.requireNonNull(status, "状态不能为null");

        return userMapper.selectByStatus(status)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        int result = userMapper.deleteById(userId);
        if (result > 0) {
            log.info("用户删除成功: {}", userId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }

        int result = userMapper.deleteBatchIds(userIds);
        if (result > 0) {
            log.info("批量删除用户成功, 数量: {}", result);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activateUser(Long userId) {
        return updateSingleUserStatus(userId, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deactivateUser(Long userId) {
        return updateSingleUserStatus(userId, 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(List<Long> userIds, Integer status) {
        if (userIds == null || userIds.isEmpty() || status == null) {
            return false;
        }

        int result = userMapper.updateUserStatus(userIds, status);
        if (result > 0) {
            log.info("批量更新用户状态成功, 数量: {}, 状态: {}", result, status);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserLoginInfo(Long userId, String loginIp) {
        Objects.requireNonNull(userId, "用户ID不能为null");
        if (StringUtils.isEmpty(loginIp)) {
            return false;
        }

        int result = userMapper.updateLoginInfo(userId, loginIp);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetUserPassword(Long userId, String newPassword) {
        Objects.requireNonNull(userId, "用户ID不能为null");
        if (StringUtils.isEmpty(newPassword)) {
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        user.setPassword(SecurityUtils.encryptPassword(newPassword));

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("用户密码重置成功: {}", user.getUsername());
            return true;
        }
        return false;
    }

    @Override
    public boolean existsUsername(String username, Long excludeUserId) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }
        return userMapper.existsUsername(username, excludeUserId);
    }

    @Override
    public boolean existsEmail(String email, Long excludeUserId) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        return userMapper.existsEmail(email, excludeUserId);
    }

    @Override
    public boolean existsPhone(String phone, Long excludeUserId) {
        if (StringUtils.isEmpty(phone)) {
            return false;
        }
        return userMapper.existsPhone(phone, excludeUserId);
    }

    @Override
    public Long getUserCount() {
        return userMapper.selectUserCount();
    }

    @Override
    public Long getActiveUserCount() {
        return userMapper.selectActiveUserCount();
    }

    @Override
    public boolean validatePassword(Long userId, String password) {
        Objects.requireNonNull(userId, "用户ID不能为null");
        if (StringUtils.isEmpty(password)) {
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        return SecurityUtils.matchesPassword(password, user.getPassword());
    }

    // Private helper methods
    private void validateUserUniqueness(String username, String email, String phone, Long excludeUserId) {
        if (existsUsername(username, excludeUserId)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (existsEmail(email, excludeUserId)) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        if (existsPhone(phone, excludeUserId)) {
            throw new IllegalArgumentException("手机号已存在");
        }
    }

    private User convertToEntity(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setGender(request.gender());
        user.setStatus(0); // 默认正常状态
        user.setDelFlag(0); // 默认未删除
        return user;
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.full(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getGender(),
                null, // deptName - 已从实体移除
                null, // avatar - 已从实体移除
                user.getStatus(),
                null, // remark - 已从实体移除
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    private void updateUserFields(User user, UserUpdateRequest request) {
        request.nickname().ifPresent(user::setNickname);
        request.email().ifPresent(user::setEmail);
        request.phone().ifPresent(user::setPhone);
        request.gender().ifPresent(user::setGender);
        request.status().ifPresent(user::setStatus);
    }

    private boolean updateSingleUserStatus(Long userId, Integer status) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        user.setStatus(status);

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("用户状态更新成功: {}, 状态: {}", user.getUsername(), status);
            return true;
        }
        return false;
    }
}
