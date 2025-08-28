package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xypai.user.converter.AppUserConverter;
import com.xypai.user.domain.entity.AppUser;
import com.xypai.user.domain.record.AppUserQueryRequest;
import com.xypai.user.domain.record.AppUserRegisterRequest;
import com.xypai.user.domain.record.AppUserResponse;
import com.xypai.user.domain.record.AppUserUpdateRequest;
import com.xypai.user.mapper.AppUserMapper;
import com.xypai.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * APP用户服务实现类
 *
 * @author XyPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserMapper appUserMapper;
    private final AppUserConverter appUserConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppUserResponse register(AppUserRegisterRequest request) {
        Objects.requireNonNull(request, "注册请求不能为null");

        // 检查手机号是否已注册
        if (existsByMobile(request.mobile())) {
            throw new IllegalArgumentException("手机号已被注册");
        }

        // 检查用户名是否已存在
        if (request.username() != null && existsByUsername(request.username(), null)) {
            throw new IllegalArgumentException("用户名已被使用");
        }

        // 转换并保存
        AppUser appUser = appUserConverter.toEntity(request);
        int result = appUserMapper.insert(appUser);

        if (result <= 0) {
            throw new RuntimeException("用户注册失败");
        }

        log.info("APP用户注册成功：mobile={}, clientType={}", request.mobile(), request.clientType());
        return appUserConverter.toResponse(appUser);
    }

    @Override
    public Optional<AppUserResponse> getByMobile(String mobile) {
        Objects.requireNonNull(mobile, "手机号不能为null");

        return appUserMapper.findByMobile(mobile)
                .map(appUserConverter::toResponse);
    }

    @Override
    public Optional<AppUserResponse> getById(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        AppUser appUser = appUserMapper.selectById(userId);
        return Optional.ofNullable(appUser)
                .map(appUserConverter::toResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<AppUserResponse> updateProfile(AppUserUpdateRequest request) {
        Objects.requireNonNull(request, "更新请求不能为null");

        AppUser appUser = appUserMapper.selectById(request.userId());
        if (appUser == null) {
            return Optional.empty();
        }

        // 检查用户名是否被其他用户使用
        if (request.username() != null &&
                existsByUsername(request.username(), request.userId())) {
            throw new IllegalArgumentException("用户名已被其他用户使用");
        }

        // 应用更新
        appUserConverter.updateEntity(appUser, request);

        int result = appUserMapper.updateById(appUser);
        if (result <= 0) {
            throw new RuntimeException("用户信息更新失败");
        }

        log.info("APP用户信息更新成功：userId={}", request.userId());
        return Optional.of(appUserConverter.toResponse(appUser));
    }

    @Override
    public IPage<AppUserResponse> getUserPage(AppUserQueryRequest request) {
        Objects.requireNonNull(request, "查询请求不能为null");

        Page<AppUser> page = new Page<>(request.pageNum(), request.pageSize());
        LambdaQueryWrapper<AppUser> queryWrapper = buildQueryWrapper(request);

        IPage<AppUser> userPage = appUserMapper.selectPage(page, queryWrapper);

        return userPage.convert(appUserConverter::toResponse);
    }

    @Override
    public List<AppUserResponse> getUsersByStatus(Integer status) {
        Objects.requireNonNull(status, "状态不能为null");

        return appUserMapper.findByStatus(status)
                .stream()
                .map(appUserConverter::toResponse)
                .toList();
    }

    @Override
    public List<AppUserResponse> getUsersByClientType(String clientType) {
        Objects.requireNonNull(clientType, "客户端类型不能为null");

        return appUserMapper.findByClientType(clientType)
                .stream()
                .map(appUserConverter::toResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long userId, Integer status) {
        Objects.requireNonNull(userId, "用户ID不能为null");
        Objects.requireNonNull(status, "状态不能为null");

        AppUser appUser = appUserMapper.selectById(userId);
        if (appUser == null) {
            return false;
        }

        appUserConverter.updateStatus(appUser, status);

        int result = appUserMapper.updateById(appUser);
        boolean success = result > 0;

        if (success) {
            log.info("APP用户状态更新成功：userId={}, status={}", userId, status);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLastLoginTime(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        AppUser appUser = appUserMapper.selectById(userId);
        if (appUser == null) {
            return false;
        }

        appUserConverter.updateLastLoginTime(appUser);

        int result = appUserMapper.updateById(appUser);
        return result > 0;
    }

    @Override
    public Boolean existsByMobile(String mobile) {
        Objects.requireNonNull(mobile, "手机号不能为null");

        Long count = appUserMapper.countByMobile(mobile);
        return count > 0;
    }

    @Override
    public Boolean existsByUsername(String username, Long excludeId) {
        Objects.requireNonNull(username, "用户名不能为null");

        Long count = appUserMapper.countByUsername(username, excludeId);
        return count > 0;
    }

    @Override
    public Long getTotalCount() {
        return appUserMapper.selectCount(null);
    }

    @Override
    public Long getActiveCount() {
        return appUserMapper.countActiveUsers();
    }

    @Override
    public Long getDisabledCount() {
        return appUserMapper.countDisabledUsers();
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<AppUser> buildQueryWrapper(AppUserQueryRequest request) {
        LambdaQueryWrapper<AppUser> queryWrapper = new LambdaQueryWrapper<>();

        // 手机号精确匹配
        if (request.mobile() != null) {
            queryWrapper.eq(AppUser::getMobile, request.mobile());
        }

        // 用户名模糊匹配
        if (request.username() != null) {
            queryWrapper.like(AppUser::getUsername, request.username());
        }

        // 昵称模糊匹配
        if (request.nickname() != null) {
            queryWrapper.like(AppUser::getNickname, request.nickname());
        }

        // 性别筛选
        if (request.gender() != null) {
            queryWrapper.eq(AppUser::getGender, request.gender());
        }

        // 状态筛选
        if (request.status() != null) {
            queryWrapper.eq(AppUser::getStatus, request.status());
        }

        // 生日范围筛选
        if (request.birthStartDate() != null) {
            queryWrapper.ge(AppUser::getBirthDate, request.birthStartDate());
        }
        if (request.birthEndDate() != null) {
            queryWrapper.le(AppUser::getBirthDate, request.birthEndDate());
        }

        // 客户端类型筛选
        if (request.clientType() != null) {
            queryWrapper.eq(AppUser::getClientType, request.clientType());
        }

        // 按注册时间降序排列
        queryWrapper.orderByDesc(AppUser::getRegisterTime);

        return queryWrapper;
    }

    // ========================================
    // 软删除相关方法实现
    // ========================================

    @Override
    @Transactional
    public Boolean softDeleteUser(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        // 检查用户是否存在且未删除
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.isDeleted()) {  // 使用现代化方法
            throw new RuntimeException("用户已删除");
        }

        // 使用现代化的业务方法 + 数据库更新
        user.softDelete();
        int result = appUserMapper.updateById(user);
        return result > 0;
    }

    @Override
    @Transactional
    public Boolean restoreUser(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        // 检查用户是否存在且已删除
        AppUser user = appUserMapper.selectById(userId);
        if (user == null || !user.isDeleted()) {
            throw new RuntimeException("用户不存在或未删除");
        }

        // 使用现代化的业务方法 + 数据库更新
        user.restore();
        int result = appUserMapper.updateById(user);
        return result > 0;
    }

    @Override
    public List<AppUserResponse> getDeletedUsers() {
        List<AppUser> deletedUsers = appUserMapper.findDeletedUsers();
        return deletedUsers.stream()
                .map(appUserConverter::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppUserResponse> getDeletedUsers(int pageNum, int pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        List<AppUser> deletedUsers = appUserMapper.findDeletedUsersWithPage(offset, pageSize);
        return deletedUsers.stream()
                .map(appUserConverter::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long getDeletedCount() {
        return appUserMapper.countDeletedUsers();
    }

    @Override
    public Optional<AppUserResponse> getDeletedById(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        return appUserMapper.findDeletedById(userId)
                .map(appUserConverter::toResponse);
    }

    @Override
    @Transactional
    public Boolean physicalDeleteUser(Long userId) {
        Objects.requireNonNull(userId, "用户ID不能为null");

        // 检查用户是否存在
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 物理删除（危险操作）
        int result = appUserMapper.physicalDeleteById(userId);
        return result > 0;
    }
}
