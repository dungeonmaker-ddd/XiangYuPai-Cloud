package com.xypai.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xypai.user.domain.dto.UserAddDTO;
import com.xypai.user.domain.dto.UserQueryDTO;
import com.xypai.user.domain.dto.UserUpdateDTO;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.vo.UserDetailVO;
import com.xypai.user.domain.vo.UserListVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 🏗️ 用户服务接口 - 企业架构实现
 * <p>
 * 遵循企业微服务架构规范：
 * - 继承MyBatis Plus IService
 * - 定义完整的业务方法
 * - 支持缓存和事务
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public interface IUserService extends IService<User> {

    // ================================
    // 🚀 核心业务方法
    // ================================

    /**
     * 注册新用户
     *
     * @param addDTO 用户创建DTO
     * @return 创建的用户实体
     */
    User registerUser(UserAddDTO addDTO);

    /**
     * 更新用户信息
     *
     * @param updateDTO 用户更新DTO
     * @return 是否成功
     */
    boolean updateUser(UserUpdateDTO updateDTO);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情VO
     */
    UserDetailVO getUserDetail(Long userId);

    // ================================
    // 🔍 查询方法
    // ================================

    /**
     * 分页查询用户列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 用户列表分页数据
     */
    IPage<UserListVO> selectUserList(Page<UserListVO> page, UserQueryDTO query);

    /**
     * 根据用户编码查询用户
     *
     * @param userCode 用户编码
     * @return 用户实体(Optional)
     */
    Optional<User> findByUserCode(String userCode);

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户实体(Optional)
     */
    Optional<User> findByMobile(String mobile);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体(Optional)
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体(Optional)
     */
    Optional<User> findByEmail(String email);

    // ================================
    // 🎯 特殊查询方法
    // ================================

    /**
     * 查询VIP用户
     *
     * @param page 分页参数
     * @return VIP用户分页数据
     */
    IPage<UserListVO> findVipUsers(Page<UserListVO> page);

    /**
     * 查询活跃用户
     *
     * @param days 最近几天
     * @param page 分页参数
     * @return 活跃用户分页数据
     */
    IPage<UserListVO> findActiveUsers(Integer days, Page<UserListVO> page);

    /**
     * 查询新用户
     *
     * @param days 最近几天
     * @param page 分页参数
     * @return 新用户分页数据
     */
    IPage<UserListVO> findNewUsers(Integer days, Page<UserListVO> page);

    // ================================
    // 🔄 状态管理方法
    // ================================

    /**
     * 批量更新用户状态
     *
     * @param userIds    用户ID列表
     * @param newStatus  新状态
     * @param operatorId 操作员ID
     * @return 更新数量
     */
    int batchUpdateStatus(List<Long> userIds, Integer newStatus, String operatorId);

    /**
     * 升级用户类型
     *
     * @param userId     用户ID
     * @param userType   用户类型
     * @param operatorId 操作员ID
     * @return 是否成功
     */
    boolean upgradeUserType(Long userId, Integer userType, String operatorId);

    /**
     * 实名认证
     *
     * @param userId     用户ID
     * @param realName   真实姓名
     * @param operatorId 操作员ID
     * @return 是否成功
     */
    boolean verifyRealName(Long userId, String realName, String operatorId);

    /**
     * 更新登录信息
     *
     * @param userId  用户ID
     * @param loginIp 登录IP
     * @return 是否成功
     */
    boolean updateLoginInfo(Long userId, String loginIp);

    // ================================
    // 📊 统计方法
    // ================================

    /**
     * 统计所有用户数量
     *
     * @return 用户总数
     */
    Long countAllUsers();

    /**
     * 统计活跃用户数量
     *
     * @return 活跃用户数
     */
    Long countActiveUsers();

    /**
     * 统计实名认证用户数量
     *
     * @return 实名认证用户数
     */
    Long countVerifiedUsers();

    /**
     * 统计VIP用户数量
     *
     * @return VIP用户数
     */
    Long countVipUsers();

    /**
     * 按用户类型统计
     *
     * @return 用户类型分布统计
     */
    List<Map<String, Object>> countByUserType();

    /**
     * 按平台统计
     *
     * @return 平台分布统计
     */
    List<Map<String, Object>> countByPlatform();

    /**
     * 按注册渠道统计
     *
     * @return 渠道分布统计
     */
    List<Map<String, Object>> countBySourceChannel();

    /**
     * 地区分布TOP统计
     *
     * @param limit TOP数量
     * @return 地区分布统计
     */
    List<Map<String, Object>> getTopLocationStats(Integer limit);

    /**
     * 用户活跃度统计
     *
     * @return 活跃度统计数据
     */
    Map<String, Object> getUserActivityStats();

    // ================================
    // ✅ 存在性检查方法
    // ================================

    /**
     * 检查手机号是否存在
     *
     * @param mobile 手机号
     * @return 是否存在
     */
    boolean existsByMobile(String mobile);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查用户编码是否存在
     *
     * @param userCode 用户编码
     * @return 是否存在
     */
    boolean existsByUserCode(String userCode);

    // ================================
    // 🔧 辅助方法
    // ================================

    /**
     * 生成用户编码
     *
     * @return 用户编码
     */
    String generateUserCode();

    /**
     * 查询用户注册趋势
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 注册趋势数据
     */
    List<Map<String, Object>> getUserRegistrationTrend(LocalDateTime startTime, LocalDateTime endTime);
}
