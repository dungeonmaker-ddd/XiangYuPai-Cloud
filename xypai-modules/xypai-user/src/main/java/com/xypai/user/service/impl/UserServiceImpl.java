package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.core.utils.bean.BeanUtils;
import com.xypai.common.security.utils.SecurityUtils;
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
import com.xypai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserWalletMapper userWalletMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<UserListVO> selectUserList(UserQueryDTO query) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .like(StringUtils.isNotBlank(query.getUsername()), User::getUsername, query.getUsername())
                .like(StringUtils.isNotBlank(query.getMobile()), User::getMobile, query.getMobile())
                .eq(query.getStatus() != null, User::getStatus, query.getStatus())
                .between(StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime()),
                        User::getCreatedAt, query.getBeginTime(), query.getEndTime())
                .orderByDesc(User::getCreatedAt);

        List<User> users = userMapper.selectList(queryWrapper);
        List<UserListVO> result = new ArrayList<>();
        
        for (User user : users) {
            UserListVO vo = convertToListVO(user);
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public UserDetailVO selectUserById(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        return convertToDetailVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(UserAddDTO userAddDTO) {
        // 校验用户名和手机号唯一性
        if (!checkUsernameUnique(userAddDTO.getUsername(), null)) {
            throw new ServiceException("用户名已存在");
        }
        if (!checkMobileUnique(userAddDTO.getMobile(), null)) {
            throw new ServiceException("手机号已被注册");
        }

        // 创建用户基础信息
        User user = User.builder()
                .username(userAddDTO.getUsername())
                .mobile(userAddDTO.getMobile())
                .password(passwordEncoder.encode(userAddDTO.getPassword()))
                .status(userAddDTO.getStatus() != null ? userAddDTO.getStatus() : 1)
                .createdAt(LocalDateTime.now())
                .build();

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new ServiceException("创建用户失败");
        }

        // 创建用户资料扩展信息
        createUserProfile(user.getId(), userAddDTO);
        
        // 创建用户钱包
        createUserWallet(user.getId());

        log.info("创建用户成功，用户ID：{}", user.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO.getId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        User existUser = userMapper.selectById(userUpdateDTO.getId());
        if (existUser == null) {
            throw new ServiceException("用户不存在");
        }

        // 校验用户名和手机号唯一性
        if (StringUtils.isNotBlank(userUpdateDTO.getUsername()) &&
                !checkUsernameUnique(userUpdateDTO.getUsername(), userUpdateDTO.getId())) {
            throw new ServiceException("用户名已存在");
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getMobile()) &&
                !checkMobileUnique(userUpdateDTO.getMobile(), userUpdateDTO.getId())) {
            throw new ServiceException("手机号已被注册");
        }

        // 更新用户基础信息
        User updateUser = User.builder()
                .id(userUpdateDTO.getId())
                .username(userUpdateDTO.getUsername())
                .mobile(userUpdateDTO.getMobile())
                .status(userUpdateDTO.getStatus())
                .version(userUpdateDTO.getVersion())
                .build();

        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new ServiceException("更新用户失败");
        }

        // 更新用户资料信息
        updateUserProfile(userUpdateDTO.getId(), userUpdateDTO);

        log.info("更新用户成功，用户ID：{}", userUpdateDTO.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("用户ID列表不能为空");
        }

        int result = userMapper.deleteBatchIds(userIds);
        log.info("批量删除用户成功，删除数量：{}", result);
        return result > 0;
    }

    @Override
    public UserDetailVO selectCurrentUser() {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }
        return selectUserById(currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCurrentUser(UserUpdateDTO userUpdateDTO) {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }
        userUpdateDTO.setId(currentUserId);
        return updateUser(userUpdateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetUserPassword(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        String defaultPassword = "123456"; // 默认密码
        User updateUser = User.builder()
                .id(userId)
                .password(passwordEncoder.encode(defaultPassword))
                .build();

        int result = userMapper.updateById(updateUser);
        log.info("重置用户密码成功，用户ID：{}", userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer status) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }
        if (status == null) {
            throw new ServiceException("用户状态不能为空");
        }

        User updateUser = User.builder()
                .id(userId)
                .status(status)
                .build();

        int result = userMapper.updateById(updateUser);
        log.info("更新用户状态成功，用户ID：{}，状态：{}", userId, status);
        return result > 0;
    }

    @Override
    public boolean checkUsernameUnique(String username, Long userId) {
        if (StringUtils.isBlank(username)) {
            return false;
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
                .ne(userId != null, User::getId, userId);

        return userMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public boolean checkMobileUnique(String mobile, Long userId) {
        if (StringUtils.isBlank(mobile)) {
            return false;
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getMobile, mobile)
                .ne(userId != null, User::getId, userId);

        return userMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public UserDetailVO selectUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            throw new ServiceException("用户名不能为空");
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        return convertToDetailVO(user);
    }

    @Override
    public UserDetailVO selectUserByMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            throw new ServiceException("手机号不能为空");
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getMobile, mobile);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        return convertToDetailVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(UserAddDTO userAddDTO) {
        // 用户注册默认为正常状态
        userAddDTO.setStatus(1);
        return insertUser(userAddDTO);
    }

    @Override
    public boolean validatePassword(Long userId, String password) {
        if (userId == null || StringUtils.isBlank(password)) {
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String newPassword) {
        if (userId == null || StringUtils.isBlank(newPassword)) {
            throw new ServiceException("参数不能为空");
        }

        User updateUser = User.builder()
                .id(userId)
                .password(passwordEncoder.encode(newPassword))
                .build();

        int result = userMapper.updateById(updateUser);
        log.info("更新用户密码成功，用户ID：{}", userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activateUser(Long userId) {
        return updateUserStatus(userId, User.Status.NORMAL.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeUser(Long userId, String reason) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        boolean result = updateUserStatus(userId, User.Status.FROZEN.getCode());
        if (result) {
            log.info("冻结用户成功，用户ID：{}，原因：{}", userId, reason);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeUser(Long userId) {
        return updateUserStatus(userId, User.Status.NORMAL.getCode());
    }

    /**
     * 转换为列表VO
     */
    private UserListVO convertToListVO(User user) {
        UserProfile profile = userProfileMapper.selectById(user.getId());
        
        return UserListVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .mobile(user.getMobile())
                .nickname(profile != null ? profile.getNickname() : null)
                .avatar(profile != null ? profile.getAvatar() : null)
                .status(user.getStatus())
                .statusDesc(user.getStatusDesc())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 转换为详情VO
     */
    private UserDetailVO convertToDetailVO(User user) {
        UserProfile profile = userProfileMapper.selectById(user.getId());
        UserWallet wallet = userWalletMapper.selectById(user.getId());
        
        return UserDetailVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .mobile(user.getMobile())
                .nickname(profile != null ? profile.getNickname() : null)
                .avatar(profile != null ? profile.getAvatar() : null)
                .email(profile != null ? profile.getEmail() : null)
                .realName(profile != null ? profile.getRealName() : null)
                .location(profile != null ? profile.getLocation() : null)
                .bio(profile != null ? profile.getBio() : null)
                .status(user.getStatus())
                .statusDesc(user.getStatusDesc())
                .createdAt(user.getCreatedAt())
                .version(user.getVersion())
                .walletBalance(wallet != null ? wallet.getFormattedBalance() : "¥0.00")
                .followed(false) // TODO: 根据当前用户查询关注状态
                .followingCount(0L) // TODO: 查询关注数
                .followersCount(0L) // TODO: 查询粉丝数
                .build();
    }

    /**
     * 创建用户资料
     */
    private void createUserProfile(Long userId, UserAddDTO userAddDTO) {
        Map<String, Object> metadata = new HashMap<>();
        if (StringUtils.isNotBlank(userAddDTO.getEmail())) {
            metadata.put("email", userAddDTO.getEmail());
        }
        if (StringUtils.isNotBlank(userAddDTO.getRealName())) {
            metadata.put("real_name", userAddDTO.getRealName());
        }
        if (StringUtils.isNotBlank(userAddDTO.getLocation())) {
            metadata.put("location", userAddDTO.getLocation());
        }
        if (StringUtils.isNotBlank(userAddDTO.getBio())) {
            metadata.put("bio", userAddDTO.getBio());
        }

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .nickname(userAddDTO.getNickname())
                .avatar(userAddDTO.getAvatar())
                .metadata(metadata.isEmpty() ? null : metadata)
                .build();

        userProfileMapper.insert(profile);
    }

    /**
     * 更新用户资料
     */
    private void updateUserProfile(Long userId, UserUpdateDTO userUpdateDTO) {
        UserProfile existProfile = userProfileMapper.selectById(userId);
        if (existProfile == null) {
            // 如果不存在资料，则创建
            createUserProfile(userId, convertToAddDTO(userUpdateDTO));
            return;
        }

        Map<String, Object> metadata = existProfile.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        if (StringUtils.isNotBlank(userUpdateDTO.getEmail())) {
            metadata.put("email", userUpdateDTO.getEmail());
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getRealName())) {
            metadata.put("real_name", userUpdateDTO.getRealName());
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getLocation())) {
            metadata.put("location", userUpdateDTO.getLocation());
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getBio())) {
            metadata.put("bio", userUpdateDTO.getBio());
        }

        UserProfile updateProfile = UserProfile.builder()
                .userId(userId)
                .nickname(userUpdateDTO.getNickname())
                .avatar(userUpdateDTO.getAvatar())
                .metadata(metadata)
                .version(existProfile.getVersion())
                .build();

        userProfileMapper.updateById(updateProfile);
    }

    /**
     * 创建用户钱包
     */
    private void createUserWallet(Long userId) {
        UserWallet wallet = UserWallet.builder()
                .userId(userId)
                .balance(0L) // 初始余额为0
                .build();

        userWalletMapper.insert(wallet);
    }

    /**
     * 转换UpdateDTO为AddDTO（用于补充资料）
     */
    private UserAddDTO convertToAddDTO(UserUpdateDTO updateDTO) {
        return UserAddDTO.builder()
                .nickname(updateDTO.getNickname())
                .email(updateDTO.getEmail())
                .avatar(updateDTO.getAvatar())
                .realName(updateDTO.getRealName())
                .location(updateDTO.getLocation())
                .bio(updateDTO.getBio())
                .build();
    }
}
