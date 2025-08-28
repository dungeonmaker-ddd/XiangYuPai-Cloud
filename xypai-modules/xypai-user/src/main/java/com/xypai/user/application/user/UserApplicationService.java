package com.xypai.user.application.user;

import com.xypai.user.application.user.command.CreateUserCommand;
import com.xypai.user.application.user.command.UpdateUserCommand;
import com.xypai.user.domain.shared.service.DomainEventPublisher;
import com.xypai.user.domain.user.UserAggregate;
import com.xypai.user.domain.user.repository.UserRepository;
import com.xypai.user.domain.user.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 👤 用户应用服务 - 编排业务流程
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * 🔨 创建用户
     */
    @Transactional
    public UserId createUser(CreateUserCommand command) {
        log.info("创建用户开始: {}", command);

        // 验证手机号唯一性
        if (userRepository.existsByMobile(command.mobile())) {
            throw new IllegalArgumentException("手机号已存在: " + command.mobile());
        }

        // 验证用户名唯一性（如果提供）
        if (command.username() != null &&
                userRepository.existsByUsername(command.username(), null)) {
            throw new IllegalArgumentException("用户名已存在: " + command.username());
        }

        // 创建用户聚合根
        var userAggregate = UserAggregate.createUser(
                command.mobile(),
                command.nickname(),
                command.clientType()
        );

        // 更新其他信息（如果提供）
        if (command.username() != null || command.avatar() != null ||
                command.gender() != null || command.birthDate() != null) {
            userAggregate.updateProfile(
                    command.username(),
                    null, // nickname已在创建时设置
                    command.avatar(),
                    command.gender(),
                    command.birthDate()
            );
        }

        // 保存聚合根
        var savedAggregate = userRepository.save(userAggregate);
        savedAggregate.markAsCreated();

        // 发布领域事件
        eventPublisher.publishAll(savedAggregate.getDomainEvents());
        savedAggregate.clearDomainEvents();

        log.info("用户创建完成: {}", savedAggregate.getUserId());
        return savedAggregate.getUserId();
    }

    /**
     * 📝 更新用户信息
     */
    @Transactional
    public void updateUser(UpdateUserCommand command) {
        log.info("更新用户信息开始: {}", command);

        // 获取用户聚合根
        var userAggregate = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + command.userId()));

        // 验证用户名唯一性（如果更新用户名）
        if (command.username() != null &&
                userRepository.existsByUsername(command.username(), command.userId())) {
            throw new IllegalArgumentException("用户名已存在: " + command.username());
        }

        // 更新用户信息
        userAggregate.updateProfile(
                command.username(),
                command.nickname(),
                command.avatar(),
                command.gender(),
                command.birthDate()
        );

        // 保存聚合根
        userRepository.save(userAggregate);

        // 发布领域事件
        eventPublisher.publishAll(userAggregate.getDomainEvents());
        userAggregate.clearDomainEvents();

        log.info("用户信息更新完成: {}", command.userId());
    }

    /**
     * 🔓 启用用户
     */
    @Transactional
    public void enableUser(UserId userId) {
        log.info("启用用户: {}", userId);

        var userAggregate = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

        userAggregate.enable();
        userRepository.save(userAggregate);

        // 发布领域事件
        eventPublisher.publishAll(userAggregate.getDomainEvents());
        userAggregate.clearDomainEvents();

        log.info("用户启用完成: {}", userId);
    }

    /**
     * 🔒 禁用用户
     */
    @Transactional
    public void disableUser(UserId userId) {
        log.info("禁用用户: {}", userId);

        var userAggregate = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

        userAggregate.disable();
        userRepository.save(userAggregate);

        // 发布领域事件
        eventPublisher.publishAll(userAggregate.getDomainEvents());
        userAggregate.clearDomainEvents();

        log.info("用户禁用完成: {}", userId);
    }

    /**
     * 🔄 更新最后登录时间
     */
    @Transactional
    public void updateLastLogin(UserId userId) {
        var userAggregate = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

        userAggregate.updateLastLogin();
        userRepository.save(userAggregate);
    }
}
