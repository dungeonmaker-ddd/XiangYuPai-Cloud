package com.xypai.user.application.activity;

import com.xypai.user.application.activity.command.CreateActivityCommand;
import com.xypai.user.application.activity.command.JoinActivityCommand;
import com.xypai.user.application.activity.command.PublishActivityCommand;
import com.xypai.user.domain.activity.ActivityAggregate;
import com.xypai.user.domain.activity.repository.ActivityRepository;
import com.xypai.user.domain.activity.valueobject.ActivityId;
import com.xypai.user.domain.shared.service.DomainEventPublisher;
import com.xypai.user.domain.user.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 📱 活动应用服务 - 编排活动业务流程
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityApplicationService {

    private final ActivityRepository activityRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * 🔨 创建活动
     */
    @Transactional
    public ActivityId createActivity(CreateActivityCommand command) {
        log.info("创建活动开始: {}", command);

        // 创建活动聚合根
        var activityAggregate = ActivityAggregate.createActivity(
                command.organizerId(),
                command.activityInfo()
        );

        // 保存聚合根
        var savedAggregate = activityRepository.save(activityAggregate);

        // 发布领域事件
        eventPublisher.publishAll(savedAggregate.getDomainEvents());
        savedAggregate.clearDomainEvents();

        log.info("活动创建完成: {}", savedAggregate.getActivityId());
        return savedAggregate.getActivityId();
    }

    /**
     * 📢 发布活动
     */
    @Transactional
    public void publishActivity(PublishActivityCommand command) {
        log.info("发布活动开始: {}", command);

        // 获取活动聚合根
        var activityAggregate = activityRepository.findById(command.activityId())
                .orElseThrow(() -> new IllegalArgumentException("活动不存在: " + command.activityId()));

        // 发布活动
        activityAggregate.publishActivity();

        // 保存聚合根
        activityRepository.save(activityAggregate);

        // 发布领域事件
        eventPublisher.publishAll(activityAggregate.getDomainEvents());
        activityAggregate.clearDomainEvents();

        log.info("活动发布完成: {}", command.activityId());
    }

    /**
     * 👥 参与活动
     */
    @Transactional
    public void joinActivity(JoinActivityCommand command) {
        log.info("参与活动开始: {}", command);

        // 获取活动聚合根
        var activityAggregate = activityRepository.findById(command.activityId())
                .orElseThrow(() -> new IllegalArgumentException("活动不存在: " + command.activityId()));

        // 参与活动
        activityAggregate.joinActivity(command.participantId());

        // 保存聚合根
        activityRepository.save(activityAggregate);

        // 发布领域事件
        eventPublisher.publishAll(activityAggregate.getDomainEvents());
        activityAggregate.clearDomainEvents();

        log.info("活动参与完成: {} -> {}", command.participantId(), command.activityId());
    }

    /**
     * 🚫 取消活动
     */
    @Transactional
    public void cancelActivity(ActivityId activityId, UserId organizerId) {
        log.info("取消活动: activityId={}, organizerId={}", activityId, organizerId);

        // 获取活动聚合根
        var activityAggregate = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在: " + activityId));

        // 验证组织者权限
        if (!activityAggregate.getOrganizerId().equals(organizerId)) {
            throw new IllegalArgumentException("只有组织者可以取消活动");
        }

        // 取消活动
        activityAggregate.cancelActivity();

        // 保存聚合根
        activityRepository.save(activityAggregate);

        // 发布领域事件
        eventPublisher.publishAll(activityAggregate.getDomainEvents());
        activityAggregate.clearDomainEvents();

        log.info("活动取消完成: {}", activityId);
    }
}
