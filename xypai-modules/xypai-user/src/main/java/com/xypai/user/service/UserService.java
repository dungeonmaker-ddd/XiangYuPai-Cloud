package com.xypai.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xypai.user.domain.record.UserCreateRequest;
import com.xypai.user.domain.record.UserQueryRequest;
import com.xypai.user.domain.record.UserResponse;
import com.xypai.user.domain.record.UserUpdateRequest;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 *
 * @author XyPai
 */
public interface UserService {

    /**
     * 创建用户
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * 更新用户
     */
    Optional<UserResponse> updateUser(UserUpdateRequest request);

    /**
     * 根据用户ID查询用户
     */
    Optional<UserResponse> getUserById(Long userId);

    /**
     * 根据用户名查询用户
     */
    Optional<UserResponse> getUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    Optional<UserResponse> getUserByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    Optional<UserResponse> getUserByPhone(String phone);

    /**
     * 分页查询用户列表
     */
    IPage<UserResponse> getUserPage(UserQueryRequest request);

    /**
     * 根据部门ID查询用户列表
     */
    List<UserResponse> getUsersByDeptId(Long deptId);

    /**
     * 根据状态查询用户列表
     */
    List<UserResponse> getUsersByStatus(Integer status);

    /**
     * 删除用户（逻辑删除）
     */
    boolean deleteUser(Long userId);

    /**
     * 批量删除用户（逻辑删除）
     */
    boolean deleteUsers(List<Long> userIds);

    /**
     * 激活用户
     */
    boolean activateUser(Long userId);

    /**
     * 停用用户
     */
    boolean deactivateUser(Long userId);

    /**
     * 批量更新用户状态
     */
    boolean updateUserStatus(List<Long> userIds, Integer status);

    /**
     * 更新用户登录信息
     */
    boolean updateUserLoginInfo(Long userId, String loginIp);

    /**
     * 重置用户密码
     */
    boolean resetUserPassword(Long userId, String newPassword);

    /**
     * 检查用户名是否存在
     */
    boolean existsUsername(String username, Long excludeUserId);

    /**
     * 检查邮箱是否存在
     */
    boolean existsEmail(String email, Long excludeUserId);

    /**
     * 检查手机号是否存在
     */
    boolean existsPhone(String phone, Long excludeUserId);

    /**
     * 获取用户总数
     */
    Long getUserCount();

    /**
     * 获取活跃用户总数
     */
    Long getActiveUserCount();

    /**
     * 验证用户密码
     */
    boolean validatePassword(Long userId, String password);
}
