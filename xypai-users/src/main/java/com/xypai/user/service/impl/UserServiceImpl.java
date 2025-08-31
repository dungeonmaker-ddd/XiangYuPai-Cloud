package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.core.utils.uuid.IdUtils;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.redis.service.RedisService;
import com.xypai.user.constant.UserConstants;
import com.xypai.user.domain.dto.UserAddDTO;
import com.xypai.user.domain.dto.UserQueryDTO;
import com.xypai.user.domain.dto.UserUpdateDTO;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.vo.UserDetailVO;
import com.xypai.user.domain.vo.UserListVO;
import com.xypai.user.enums.Gender;
import com.xypai.user.enums.UserStatus;
import com.xypai.user.enums.UserType;
import com.xypai.user.mapper.UserMapper;
import com.xypai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * 🏗️ 用户服务实现类 - 企业架构实现 (MP QueryWrapper版本)
 * <p>
 * 遵循企业微服务架构规范：
 * - 继承MyBatis Plus ServiceImpl，获得完整CRUD功能
 * - 使用LambdaQueryWrapper和QueryWrapper进行条件查询
 * - 使用RedisService进行缓存管理
 * - 事务注解和日志注解
 * - 完整的异常处理和业务规则验证
 * - 大幅简化查询逻辑，提高开发效率
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;
    private final RedisService redisService;

    // ================================
    // 🚀 核心业务方法
    // ================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log(title = "用户注册", businessType = BusinessType.INSERT)
    public User registerUser(UserAddDTO addDTO) {
        // 1. 业务规则验证
        validateForRegister(addDTO);

        // 2. 创建用户实体
        User user = buildUserFromAddDTO(addDTO);

        // 3. 保存到数据库
        boolean result = save(user);
        if (!result) {
            throw new ServiceException("用户注册失败");
        }

        // 4. 清除相关缓存
        clearUserCache(user);

        // 5. 记录日志
        log.info("用户注册成功: userId={}, userCode={}, mobile={}",
                user.getUserId(), user.getUserCode(), user.getMobile());

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log(title = "用户信息更新", businessType = BusinessType.UPDATE)
    public boolean updateUser(UserUpdateDTO updateDTO) {
        // 1. 获取原用户信息
        User existingUser = getById(updateDTO.userId());
        if (existingUser == null) {
            throw new ServiceException("用户不存在");
        }

        // 2. 版本号检查(乐观锁)
        if (!existingUser.getVersion().equals(updateDTO.version())) {
            throw new ServiceException("数据已被其他用户修改，请刷新后重试");
        }

        // 3. 构建更新对象
        User updateUser = buildUserFromUpdateDTO(updateDTO, existingUser);

        // 4. 执行更新
        boolean result = updateById(updateUser);
        if (result) {
            // 清除缓存
            clearUserCache(updateUser);
            log.info("用户信息更新成功: userId={}", updateDTO.userId());
        }

        return result;
    }

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        // 1. 尝试从缓存获取
        String cacheKey = UserConstants.USER_CACHE_KEY + userId;
        UserDetailVO cached = redisService.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 🔥 使用 LambdaQueryWrapper 从数据库查询
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, userId)
                .eq(User::getDelFlag, "0"));

        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 3. 转换为VO并补充描述信息
        UserDetailVO userDetail = convertToUserDetailVO(user);

        // 4. 缓存结果
        redisService.setCacheObject(cacheKey, userDetail,
                (long) UserConstants.USER_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return userDetail;
    }

    // ================================
    // 🔍 查询方法
    // ================================

    @Override
    public IPage<UserListVO> selectUserList(Page<UserListVO> page, UserQueryDTO query) {
        // 🔥 使用 LambdaQueryWrapper 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(query.userId() != null, User::getUserId, query.userId())
                .eq(StringUtils.isNotEmpty(query.userCode()), User::getUserCode, query.userCode())
                .like(StringUtils.isNotEmpty(query.mobile()), User::getMobile, query.mobile())
                .like(StringUtils.isNotEmpty(query.username()), User::getUsername, query.username())
                .like(StringUtils.isNotEmpty(query.nickname()), User::getNickname, query.nickname())
                .like(StringUtils.isNotEmpty(query.email()), User::getEmail, query.email())
                .eq(query.gender() != null, User::getGender, query.gender())
                .eq(query.status() != null, User::getStatus, query.status())
                .eq(query.userType() != null, User::getUserType, query.userType())
                .eq(query.isVerified() != null, User::getIsVerified, query.isVerified())
                .eq(StringUtils.isNotEmpty(query.platform()), User::getPlatform, query.platform())
                .eq(StringUtils.isNotEmpty(query.sourceChannel()), User::getSourceChannel, query.sourceChannel())
                .like(StringUtils.isNotEmpty(query.location()), User::getLocation, query.location())
                .eq(query.deptId() != null, User::getDeptId, query.deptId())
                .ge(query.minUserLevel() != null, User::getUserLevel, query.minUserLevel())
                .le(query.maxUserLevel() != null, User::getUserLevel, query.maxUserLevel())
                .ge(query.createTimeStart() != null, User::getCreateTime, query.createTimeStart())
                .le(query.createTimeEnd() != null, User::getCreateTime, query.createTimeEnd())
                .ge(query.lastLoginTimeStart() != null, User::getLastLoginTime, query.lastLoginTimeStart())
                .le(query.lastLoginTimeEnd() != null, User::getLastLoginTime, query.lastLoginTimeEnd())
                .orderByDesc(User::getCreateTime);

        // 🚀 执行分页查询
        IPage<User> userPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);

        // 🔄 转换为VO对象
        IPage<UserListVO> result = userPage.convert(this::convertToUserListVO);

        return result;
    }

    @Override
    public Optional<User> findByUserCode(String userCode) {
        if (StringUtils.isEmpty(userCode)) {
            return Optional.empty();
        }

        // 尝试从缓存获取
        String cacheKey = UserConstants.USER_CODE_CACHE_KEY + userCode;
        User cached = redisService.getCacheObject(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }

        // 🔥 使用 LambdaQueryWrapper 查询
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserCode, userCode)
                .eq(User::getDelFlag, "0"));

        if (user != null) {
            redisService.setCacheObject(cacheKey, user,
                    (long) UserConstants.USER_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return Optional.empty();
        }

        // 🔥 使用 LambdaQueryWrapper 查询
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getMobile, mobile)
                .eq(User::getDelFlag, "0"));

        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return Optional.empty();
        }

        // 🔥 使用 LambdaQueryWrapper 查询
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDelFlag, "0"));

        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return Optional.empty();
        }

        // 🔥 使用 LambdaQueryWrapper 查询
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getDelFlag, "0"));

        return Optional.ofNullable(user);
    }

    // ================================
    // 🎯 特殊查询方法
    // ================================

    @Override
    public IPage<UserListVO> findVipUsers(Page<UserListVO> page) {
        // 🔥 使用 LambdaQueryWrapper 查询VIP用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .ge(User::getUserType, 1)  // VIP用户类型 >= 1
                .eq(User::getDelFlag, "0")
                .eq(User::getStatus, 1)    // 正常状态
                .orderByDesc(User::getUserType)
                .orderByDesc(User::getCreateTime);

        IPage<User> userPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return userPage.convert(this::convertToUserListVO);
    }

    @Override
    public IPage<UserListVO> findActiveUsers(Integer days, Page<UserListVO> page) {
        // 🔥 使用 LambdaQueryWrapper 查询活跃用户
        LocalDateTime activeTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .ge(User::getLastLoginTime, activeTime)
                .eq(User::getDelFlag, "0")
                .eq(User::getStatus, 1)
                .orderByDesc(User::getLastLoginTime);

        IPage<User> userPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return userPage.convert(this::convertToUserListVO);
    }

    @Override
    public IPage<UserListVO> findNewUsers(Integer days, Page<UserListVO> page) {
        // 🔥 使用 LambdaQueryWrapper 查询新用户
        LocalDateTime newUserTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, newUserTime)
                .eq(User::getDelFlag, "0")
                .orderByDesc(User::getCreateTime);

        IPage<User> userPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return userPage.convert(this::convertToUserListVO);
    }

    // ================================
    // 🔄 状态管理方法
    // ================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log(title = "批量更新用户状态", businessType = BusinessType.UPDATE)
    public int batchUpdateStatus(List<Long> userIds, Integer newStatus, String operatorId) {
        // 验证参数
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("用户ID列表不能为空");
        }

        if (userIds.size() > UserConstants.BATCH_OPERATION_MAX_SIZE) {
            throw new ServiceException("批量操作数量不能超过" + UserConstants.BATCH_OPERATION_MAX_SIZE);
        }

        // 执行更新
        int updated = userMapper.batchUpdateStatus(userIds, newStatus, operatorId);

        // 清除缓存
        userIds.forEach(userId -> {
            redisService.deleteObject(UserConstants.USER_CACHE_KEY + userId);
        });

        log.info("批量更新用户状态完成: userIds={}, newStatus={}, updated={}",
                userIds, newStatus, updated);

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log(title = "用户类型升级", businessType = BusinessType.UPDATE)
    public boolean upgradeUserType(Long userId, Integer userType, String operatorId) {
        // 🔥 使用 LambdaUpdateWrapper 更新用户类型
        boolean updated = update(new LambdaUpdateWrapper<User>()
                .set(User::getUserType, userType)
                .set(User::getUpdateBy, operatorId)
                .set(User::getUpdateTime, LocalDateTime.now())
                .eq(User::getUserId, userId)
                .eq(User::getDelFlag, "0"));

        if (updated) {
            // 清除缓存
            redisService.deleteObject(UserConstants.USER_CACHE_KEY + userId);
            log.info("用户类型升级成功: userId={}, newUserType={}", userId, userType);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log(title = "用户实名认证", businessType = BusinessType.UPDATE)
    public boolean verifyRealName(Long userId, String realName, String operatorId) {
        // 🔥 使用 LambdaUpdateWrapper 进行实名认证
        boolean updated = update(new LambdaUpdateWrapper<User>()
                .set(User::getRealName, realName)
                .set(User::getIsVerified, 1)
                .set(User::getUpdateBy, operatorId)
                .set(User::getUpdateTime, LocalDateTime.now())
                .eq(User::getUserId, userId)
                .eq(User::getDelFlag, "0"));

        if (updated) {
            // 清除缓存
            redisService.deleteObject(UserConstants.USER_CACHE_KEY + userId);
            log.info("用户实名认证成功: userId={}, realName={}", userId, realName);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLoginInfo(Long userId, String loginIp) {
        int updated = userMapper.updateLoginInfo(userId, loginIp);

        if (updated > 0) {
            // 清除缓存
            redisService.deleteObject(UserConstants.USER_CACHE_KEY + userId);
        }

        return updated > 0;
    }

    // ================================
    // 📊 统计方法
    // ================================

    @Override
    public Long countAllUsers() {
        String cacheKey = UserConstants.USER_STATS_CACHE_KEY + "total";
        Long cached = redisService.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 🔥 使用 LambdaQueryWrapper 统计用户总数
        Long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getDelFlag, "0"));

        redisService.setCacheObject(cacheKey, count,
                (long) UserConstants.STATS_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return count;
    }

    @Override
    public Long countActiveUsers() {
        // 🔥 使用 LambdaQueryWrapper 统计活跃用户
        LocalDateTime activeTime = LocalDateTime.now().minusDays(30);

        return count(new LambdaQueryWrapper<User>()
                .ge(User::getLastLoginTime, activeTime)
                .eq(User::getDelFlag, "0")
                .eq(User::getStatus, 1));
    }

    @Override
    public Long countVerifiedUsers() {
        // 🔥 使用 LambdaQueryWrapper 统计实名认证用户
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getIsVerified, 1)
                .eq(User::getDelFlag, "0"));
    }

    @Override
    public Long countVipUsers() {
        // 🔥 使用 LambdaQueryWrapper 统计VIP用户
        return count(new LambdaQueryWrapper<User>()
                .ge(User::getUserType, 1)
                .eq(User::getDelFlag, "0")
                .eq(User::getStatus, 1));
    }

    @Override
    public List<Map<String, Object>> countByUserType() {
        return userMapper.countByUserType();
    }

    @Override
    public List<Map<String, Object>> countByPlatform() {
        return userMapper.countByPlatform();
    }

    @Override
    public List<Map<String, Object>> countBySourceChannel() {
        return userMapper.countBySourceChannel();
    }

    @Override
    public List<Map<String, Object>> getTopLocationStats(Integer limit) {
        return userMapper.getTopLocationStats(limit);
    }

    @Override
    public Map<String, Object> getUserActivityStats() {
        return userMapper.getUserActivityStats();
    }

    // ================================
    // ✅ 存在性检查方法
    // ================================

    @Override
    public boolean existsByMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }

        // 🔥 使用 LambdaQueryWrapper 检查手机号是否存在
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getMobile, mobile)
                .eq(User::getDelFlag, "0")) > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }

        // 🔥 使用 LambdaQueryWrapper 检查用户名是否存在
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDelFlag, "0")) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }

        // 🔥 使用 LambdaQueryWrapper 检查邮箱是否存在
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getDelFlag, "0")) > 0;
    }

    @Override
    public boolean existsByUserCode(String userCode) {
        if (StringUtils.isEmpty(userCode)) {
            return false;
        }

        // 🔥 使用 LambdaQueryWrapper 检查用户编码是否存在
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getUserCode, userCode)
                .eq(User::getDelFlag, "0")) > 0;
    }

    // ================================
    // 🔧 辅助方法
    // ================================

    @Override
    public String generateUserCode() {
        // 尝试从数据库生成
        String userCode = userMapper.generateNextUserCode();
        if (StringUtils.isNotEmpty(userCode)) {
            return userCode;
        }

        // 备用生成方案：前缀 + 当前日期 + 随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = IdUtils.fastSimpleUUID().substring(0, 8).toUpperCase();

        return UserConstants.USER_CODE_PREFIX + dateStr + randomStr;
    }

    @Override
    public List<Map<String, Object>> getUserRegistrationTrend(LocalDateTime startTime, LocalDateTime endTime) {
        return userMapper.getUserRegistrationTrend(startTime, endTime);
    }

    // ================================
    // 🔧 私有辅助方法
    // ================================

    /**
     * 注册时的业务规则验证
     */
    private void validateForRegister(UserAddDTO addDTO) {
        // 检查手机号是否已存在
        if (existsByMobile(addDTO.mobile())) {
            throw new ServiceException("手机号已被注册");
        }

        // 检查用户名是否已存在
        if (existsByUsername(addDTO.username())) {
            throw new ServiceException("用户名已被使用");
        }

        // 检查邮箱是否已存在(如果提供了邮箱)
        if (StringUtils.isNotEmpty(addDTO.email()) && existsByEmail(addDTO.email())) {
            throw new ServiceException("邮箱已被使用");
        }
    }

    /**
     * 从AddDTO构建User实体
     */
    private User buildUserFromAddDTO(UserAddDTO addDTO) {
        User user = new User();

        // 基本信息
        user.setUserCode(generateUserCode());
        user.setMobile(addDTO.mobile());
        user.setUsername(addDTO.username());
        user.setNickname(addDTO.nickname());
        user.setEmail(addDTO.email());
        user.setGender(addDTO.gender());
        user.setLocation(addDTO.location());
        user.setPlatform(addDTO.platform());
        user.setSourceChannel(addDTO.sourceChannel());
        user.setBio(addDTO.bio());
        user.setDeptId(addDTO.deptId());

        // 默认值设置
        user.setAvatarUrl(UserConstants.DEFAULT_AVATAR_URL);
        user.setStatus(UserConstants.USER_STATUS_NORMAL);
        user.setUserType(UserConstants.USER_TYPE_NORMAL);
        user.setIsVerified(UserConstants.VERIFIED_NO);
        user.setUserLevel(UserConstants.INITIAL_USER_LEVEL);
        user.setUserPoints(UserConstants.INITIAL_USER_POINTS);
        user.setBalance(UserConstants.INITIAL_BALANCE);
        user.setLoginCount(0);
        user.setDelFlag("0");
        user.setVersion(1);

        return user;
    }

    /**
     * 从UpdateDTO构建User实体
     */
    private User buildUserFromUpdateDTO(UserUpdateDTO updateDTO, User existingUser) {
        User user = new User();
        user.setUserId(updateDTO.userId());
        user.setVersion(updateDTO.version());

        // 只更新提供的字段
        if (StringUtils.isNotEmpty(updateDTO.nickname())) {
            user.setNickname(updateDTO.nickname());
        }
        if (StringUtils.isNotEmpty(updateDTO.email())) {
            // 检查新邮箱是否已被其他用户使用
            if (!updateDTO.email().equals(existingUser.getEmail()) && existsByEmail(updateDTO.email())) {
                throw new ServiceException("邮箱已被其他用户使用");
            }
            user.setEmail(updateDTO.email());
        }
        if (updateDTO.gender() != null) {
            user.setGender(updateDTO.gender());
        }
        if (StringUtils.isNotEmpty(updateDTO.avatarUrl())) {
            user.setAvatarUrl(updateDTO.avatarUrl());
        }
        if (StringUtils.isNotEmpty(updateDTO.location())) {
            user.setLocation(updateDTO.location());
        }
        if (StringUtils.isNotEmpty(updateDTO.bio())) {
            user.setBio(updateDTO.bio());
        }

        return user;
    }


    /**
     * 将User实体转换为UserListVO
     *
     * @param user 用户实体
     * @return 用户列表VO
     */
    private UserListVO convertToUserListVO(User user) {
        return UserListVO.builder()
                .userId(user.getUserId())
                .userCode(user.getUserCode())
                .mobile(user.getMobile())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .genderDesc(Gender.getDescByCode(user.getGender()))
                .location(user.getLocation())
                .status(user.getStatus())
                .statusDesc(UserStatus.getDescByCode(user.getStatus()))
                .userType(user.getUserType())
                .userTypeDesc(UserType.getDescByCode(user.getUserType()))
                .isVerified(user.getIsVerified())
                .platform(user.getPlatform())
                .userLevel(user.getUserLevel())
                .userPoints(user.getUserPoints())
                .loginCount(user.getLoginCount())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime() != null ?
                        user.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    /**
     * 将User实体转换为UserDetailVO
     *
     * @param user 用户实体
     * @return 用户详情VO
     */
    private UserDetailVO convertToUserDetailVO(User user) {
        return UserDetailVO.builder()
                .userId(user.getUserId())
                .userCode(user.getUserCode())
                .mobile(user.getMobile())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .realName(user.getRealName())
                .gender(user.getGender())
                .genderDesc(Gender.getDescByCode(user.getGender()))
                .avatarUrl(user.getAvatarUrl())
                .birthday(user.getBirthday())
                .location(user.getLocation())
                .bio(user.getBio())
                .status(user.getStatus())
                .statusDesc(UserStatus.getDescByCode(user.getStatus()))
                .userType(user.getUserType())
                .userTypeDesc(UserType.getDescByCode(user.getUserType()))
                .isVerified(user.getIsVerified())
                .verifiedDesc(user.getIsVerified() == 1 ? "已认证" : "未认证")
                .platform(user.getPlatform())
                .sourceChannel(user.getSourceChannel())
                .userLevel(user.getUserLevel())
                .userPoints(user.getUserPoints())
                .balance(user.getBalance())
                .loginCount(user.getLoginCount())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createTime(user.getCreateTime() != null ?
                        user.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .updateTime(user.getUpdateTime() != null ?
                        user.getUpdateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .version(user.getVersion())
                .build();
    }

    /**
     * 清除用户相关缓存
     */
    private void clearUserCache(User user) {
        // 清除用户信息缓存
        redisService.deleteObject(UserConstants.USER_CACHE_KEY + user.getUserId());

        // 清除用户编码缓存
        if (StringUtils.isNotEmpty(user.getUserCode())) {
            redisService.deleteObject(UserConstants.USER_CODE_CACHE_KEY + user.getUserCode());
        }

        // 清除手机号缓存
        if (StringUtils.isNotEmpty(user.getMobile())) {
            redisService.deleteObject(UserConstants.USER_MOBILE_CACHE_KEY + user.getMobile());
        }

        // 清除用户名缓存
        if (StringUtils.isNotEmpty(user.getUsername())) {
            redisService.deleteObject(UserConstants.USER_USERNAME_CACHE_KEY + user.getUsername());
        }

        // 清除统计缓存
        // 清除统计缓存 - 使用具体的key删除而不是模式匹配
        redisService.deleteObject(UserConstants.USER_STATS_CACHE_KEY + "total");
    }
}
