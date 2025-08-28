package com.xypai.user.interfaces.assembler;

import com.xypai.user.domain.user.entity.AppUser;
import com.xypai.user.interfaces.dto.request.UserRegisterRequest;
import com.xypai.user.interfaces.dto.request.UserUpdateRequest;
import com.xypai.user.interfaces.dto.response.DetailedUserResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户转换器
 * 负责实体与DTO之间的转换
 *
 * @author XyPai
 */
@Component
public class UserAssembler {

    /**
     * 注册请求转实体（现代化Builder方式）
     */
    public AppUser toEntity(UserRegisterRequest request) {
        Objects.requireNonNull(request, "注册请求不能为null");

        return AppUser.builder()
                .mobile(request.mobile())
                .username(request.username())
                .nickname(request.nickname())
                .avatar(request.avatar())
                .gender(request.gender())
                .birthDate(request.birthDate())
                .clientType(request.clientType())
                .build();
    }

    /**
     * 实体转响应（增强单表版本）
     */
    public DetailedUserResponse toResponse(AppUser entity) {
        Objects.requireNonNull(entity, "用户实体不能为null");

        return DetailedUserResponse.builder()
                // 基础信息
                .userId(entity.getUserId())
                .mobile(entity.getMobile())
                .username(entity.getUsername())
                .nickname(entity.getNickname())
                .avatar(entity.getAvatar())
                .gender(entity.getGender())
                .birthDate(entity.getBirthDate())
                .status(entity.getStatus())
                .registerTime(entity.getRegisterTime())
                .lastLoginTime(entity.getLastLoginTime())
                .clientType(entity.getClientType())
                // 详细资料
                .realName(entity.getRealName())
                .email(entity.getEmail())
                .wechat(entity.getWechat())
                .occupation(entity.getOccupation())
                .location(entity.getLocation())
                .bio(entity.getBio())
                .interests(entity.getInterests())
                .height(entity.getHeight())
                .weight(entity.getWeight())
                .notificationPush(entity.getNotificationPush())
                .privacyLevel(entity.getPrivacyLevel())
                .language(entity.getLanguage())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 更新请求应用到实体（增强单表版本）
     */
    public void updateEntity(AppUser entity, UserUpdateRequest request) {
        Objects.requireNonNull(entity, "用户实体不能为null");
        Objects.requireNonNull(request, "更新请求不能为null");

        // 只更新非null字段 - 基础信息
        if (request.username() != null) {
            entity.setUsername(request.username());
        }
        if (request.nickname() != null) {
            entity.setNickname(request.nickname());
        }
        if (request.avatar() != null) {
            entity.setAvatar(request.avatar());
        }
        if (request.gender() != null) {
            entity.setGender(request.gender());
        }
        if (request.birthDate() != null) {
            entity.setBirthDate(request.birthDate());
        }

        // 详细资料
        if (request.realName() != null) {
            entity.setRealName(request.realName());
        }
        if (request.email() != null) {
            entity.setEmail(request.email());
        }
        if (request.wechat() != null) {
            entity.setWechat(request.wechat());
        }
        if (request.occupation() != null) {
            entity.setOccupation(request.occupation());
        }
        if (request.location() != null) {
            entity.setLocation(request.location());
        }
        if (request.bio() != null) {
            entity.setBio(request.bio());
        }
        if (request.interests() != null) {
            entity.setInterests(request.interests());
        }
        if (request.height() != null) {
            entity.setHeight(request.height());
        }
        if (request.weight() != null) {
            entity.setWeight(request.weight());
        }

        // 设置信息
        if (request.notificationPush() != null) {
            entity.setNotificationPush(request.notificationPush());
        }
        if (request.privacyLevel() != null) {
            entity.setPrivacyLevel(request.privacyLevel());
        }
        if (request.language() != null) {
            entity.setLanguage(request.language());
        }
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime(AppUser entity) {
        Objects.requireNonNull(entity, "用户实体不能为null");
        entity.setLastLoginTime(LocalDateTime.now());
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(AppUser entity, Integer status) {
        Objects.requireNonNull(entity, "用户实体不能为null");
        Objects.requireNonNull(status, "状态不能为null");

        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值只能是0或1");
        }

        entity.setStatus(status);
    }
}
