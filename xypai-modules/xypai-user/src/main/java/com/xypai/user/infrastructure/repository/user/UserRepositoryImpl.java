package com.xypai.user.infrastructure.repository.user;

import com.xypai.user.domain.user.UserAggregate;
import com.xypai.user.domain.user.entity.AppUser;
import com.xypai.user.domain.user.repository.UserRepository;
import com.xypai.user.domain.user.valueobject.UserId;
import com.xypai.user.mapper.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 👤 用户仓储实现 - 基础设施层
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final AppUserMapper appUserMapper;

    @Override
    public UserAggregate save(UserAggregate userAggregate) {
        // 聚合根转换为实体
        var appUser = toEntity(userAggregate);

        if (userAggregate.getUserId() == null) {
            // 新增用户
            appUserMapper.insert(appUser);
            // 重建聚合根（包含生成的ID）
            return fromEntity(appUser);
        } else {
            // 更新用户
            appUserMapper.updateById(appUser);
            return userAggregate;
        }
    }

    @Override
    public Optional<UserAggregate> findById(UserId userId) {
        var appUser = appUserMapper.selectById(userId.value());
        return appUser != null ? Optional.of(fromEntity(appUser)) : Optional.empty();
    }

    @Override
    public Optional<UserAggregate> findByMobile(String mobile) {
        var appUser = appUserMapper.selectByMobile(mobile);
        return appUser != null ? Optional.of(fromEntity(appUser)) : Optional.empty();
    }

    @Override
    public Optional<UserAggregate> findByUsername(String username) {
        var appUser = appUserMapper.selectByUsername(username);
        return appUser != null ? Optional.of(fromEntity(appUser)) : Optional.empty();
    }

    @Override
    public boolean existsByMobile(String mobile) {
        return appUserMapper.existsByMobile(mobile);
    }

    @Override
    public boolean existsByUsername(String username, UserId excludeUserId) {
        Long excludeId = excludeUserId != null ? excludeUserId.value() : null;
        return appUserMapper.existsByUsername(username, excludeId);
    }

    @Override
    public void delete(UserId userId) {
        appUserMapper.deleteById(userId.value());
    }

    // ========================================
    // 聚合根与实体转换
    // ========================================

    /**
     * 聚合根转实体
     */
    private AppUser toEntity(UserAggregate aggregate) {
        return AppUser.builder()
                .userId(aggregate.getUserId() != null ? aggregate.getUserId().value() : null)
                .mobile(aggregate.getMobile())
                .username(aggregate.getUsername())
                .nickname(aggregate.getNickname())
                .avatar(aggregate.getAvatar())
                .gender(aggregate.getGender())
                .birthDate(aggregate.getBirthDate())
                .status(aggregate.getStatus())
                .clientType(aggregate.getClientType())
                .realName(aggregate.getRealName())
                .email(aggregate.getEmail())
                .wechat(aggregate.getWechat())
                .occupation(aggregate.getOccupation())
                .location(aggregate.getLocation())
                .bio(aggregate.getBio())
                .interests(aggregate.getInterests())
                .height(aggregate.getHeight())
                .weight(aggregate.getWeight())
                .notificationPush(aggregate.getNotificationPush())
                .privacyLevel(aggregate.getPrivacyLevel())
                .language(aggregate.getLanguage())
                .registerTime(aggregate.getRegisterTime())
                .lastLoginTime(aggregate.getLastLoginTime())
                .updateTime(aggregate.getUpdateTime())
                .build();
    }

    /**
     * 实体转聚合根
     */
    private UserAggregate fromEntity(AppUser entity) {
        return UserAggregate.fromExisting(
                entity.getUserId(),
                entity.getMobile(),
                entity.getUsername(),
                entity.getNickname(),
                entity.getAvatar(),
                entity.getGender(),
                entity.getBirthDate(),
                entity.getStatus(),
                entity.getClientType(),
                entity.getRegisterTime(),
                entity.getLastLoginTime()
        );
    }
}
