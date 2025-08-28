package com.xypai.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xypai.user.domain.record.AppUserQueryRequest;
import com.xypai.user.domain.record.AppUserRegisterRequest;
import com.xypai.user.domain.record.AppUserResponse;
import com.xypai.user.domain.record.AppUserUpdateRequest;

import java.util.List;
import java.util.Optional;

/**
 * APP用户服务接口
 *
 * @author XyPai
 */
public interface AppUserService {

    /**
     * 用户注册
     */
    AppUserResponse register(AppUserRegisterRequest request);

    /**
     * 根据手机号获取用户
     */
    Optional<AppUserResponse> getByMobile(String mobile);

    /**
     * 根据用户ID获取用户
     */
    Optional<AppUserResponse> getById(Long userId);

    /**
     * 更新用户信息
     */
    Optional<AppUserResponse> updateProfile(AppUserUpdateRequest request);

    /**
     * 分页查询用户
     */
    IPage<AppUserResponse> getUserPage(AppUserQueryRequest request);

    /**
     * 根据状态查询用户列表
     */
    List<AppUserResponse> getUsersByStatus(Integer status);

    /**
     * 根据客户端类型查询用户列表
     */
    List<AppUserResponse> getUsersByClientType(String clientType);

    /**
     * 更新用户状态
     */
    Boolean updateStatus(Long userId, Integer status);

    /**
     * 更新最后登录时间
     */
    Boolean updateLastLoginTime(Long userId);

    /**
     * 检查手机号是否存在
     */
    Boolean existsByMobile(String mobile);

    /**
     * 检查用户名是否存在
     */
    Boolean existsByUsername(String username, Long excludeId);

    /**
     * 获取用户总数
     */
    Long getTotalCount();

    /**
     * 获取活跃用户数
     */
    Long getActiveCount();

    /**
     * 获取禁用用户数
     */
    Long getDisabledCount();

    // ========================================
    // 软删除相关方法
    // ========================================

    /**
     * 软删除用户
     */
    Boolean softDeleteUser(Long userId);

    /**
     * 恢复已删除用户
     */
    Boolean restoreUser(Long userId);

    /**
     * 查询已删除用户列表
     */
    List<AppUserResponse> getDeletedUsers();

    /**
     * 查询已删除用户（分页）
     */
    List<AppUserResponse> getDeletedUsers(int pageNum, int pageSize);

    /**
     * 统计已删除用户数量
     */
    Long getDeletedCount();

    /**
     * 根据ID查询已删除用户
     */
    Optional<AppUserResponse> getDeletedById(Long userId);

    /**
     * 物理删除用户（危险操作）
     */
    Boolean physicalDeleteUser(Long userId);
}
