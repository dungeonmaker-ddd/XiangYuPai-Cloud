package com.xypai.user.domain.activity;

import com.xypai.user.domain.activity.entity.ActivityInfo;
import com.xypai.user.domain.activity.entity.ActivityParticipant;
import com.xypai.user.domain.activity.entity.ActivitySettings;
import com.xypai.user.domain.activity.enums.ActivityStatus;
import com.xypai.user.domain.activity.valueobject.ActivityId;
import com.xypai.user.domain.shared.ActivityCreatedEvent;
import com.xypai.user.domain.shared.ActivityJoinedEvent;
import com.xypai.user.domain.shared.ActivityPublishedEvent;
import com.xypai.user.domain.shared.DomainEvent;
import com.xypai.user.domain.user.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 📱 活动聚合根 - 处理活动创建、管理和参与
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class ActivityAggregate {

    private final ActivityId activityId;
    private final UserId organizerId;
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private ActivityInfo info;
    private ActivityStatus status;
    private ActivitySettings settings;
    private List<ActivityParticipant> participants;
    private List<ActivityParticipant> waitingList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========================================
    // 构造器
    // ========================================

    private ActivityAggregate(
            ActivityId activityId,
            UserId organizerId,
            ActivityInfo info,
            ActivityStatus status,
            ActivitySettings settings,
            List<ActivityParticipant> participants,
            List<ActivityParticipant> waitingList,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {
        this.activityId = Objects.requireNonNull(activityId, "活动ID不能为空");
        this.organizerId = Objects.requireNonNull(organizerId, "组织者ID不能为空");
        this.info = Objects.requireNonNull(info, "活动信息不能为空");
        this.status = Objects.requireNonNull(status, "活动状态不能为空");
        this.settings = Objects.requireNonNull(settings, "活动设置不能为空");
        this.participants = new ArrayList<>(Objects.requireNonNull(participants, "参与者列表不能为空"));
        this.waitingList = new ArrayList<>(Objects.requireNonNull(waitingList, "候补列表不能为空"));
        this.createTime = Objects.requireNonNull(createTime, "创建时间不能为空");
        this.updateTime = updateTime;
    }

    // ========================================
    // 静态工厂方法
    // ========================================

    /**
     * 创建新的活动
     */
    public static ActivityAggregate createActivity(
            UserId organizerId,
            ActivityInfo info,
            ActivitySettings settings
    ) {
        Objects.requireNonNull(organizerId, "组织者ID不能为空");
        Objects.requireNonNull(info, "活动信息不能为空");
        Objects.requireNonNull(settings, "活动设置不能为空");

        var activityId = ActivityId.generate();
        var createTime = LocalDateTime.now();

        var aggregate = new ActivityAggregate(
                activityId,
                organizerId,
                info,
                ActivityStatus.DRAFT,
                settings,
                new ArrayList<>(),
                new ArrayList<>(),
                createTime,
                createTime
        );

        // 发布活动创建事件
        var event = ActivityCreatedEvent.create(activityId, organizerId, info.type());
        aggregate.addDomainEvent(event);

        return aggregate;
    }

    /**
     * 从持久化数据重构聚合根
     */
    public static ActivityAggregate reconstruct(
            ActivityId activityId,
            UserId organizerId,
            ActivityInfo info,
            ActivityStatus status,
            ActivitySettings settings,
            List<ActivityParticipant> participants,
            List<ActivityParticipant> waitingList,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {
        return new ActivityAggregate(
                activityId,
                organizerId,
                info,
                status,
                settings,
                participants,
                waitingList,
                createTime,
                updateTime
        );
    }

    // ========================================
    // 业务方法
    // ========================================

    /**
     * 🎯 业务规则：发布活动
     */
    public DomainEvent publishActivity() {
        validateCanPublish();

        this.status = ActivityStatus.PUBLISHED;
        this.updateTime = LocalDateTime.now();

        var event = ActivityPublishedEvent.create(activityId, organizerId, info.startTime());
        addDomainEvent(event);
        return event;
    }

    /**
     * 🎯 业务规则：用户参与活动
     */
    public DomainEvent joinActivity(UserId participantId, String joinReason) {
        validateCanJoin(participantId);

        var participant = settings.requiresApproval()
                ? ActivityParticipant.create(participantId, joinReason)
                : ActivityParticipant.createApproved(participantId, joinReason);

        // 检查是否达到参与人数上限
        if (getApprovedParticipantCount() >= info.maxParticipants()) {
            if (settings.enableWaitingList() && waitingList.size() < settings.maxWaitingList()) {
                // 加入候补列表
                waitingList.add(participant);
                // TODO: 创建候补列表加入事件
                throw new IllegalStateException("活动参与人数已满，已加入候补列表");
            } else {
                throw new IllegalStateException("活动参与人数已满，且候补列表已满");
            }
        }

        participants.add(participant);
        this.updateTime = LocalDateTime.now();

        var event = ActivityJoinedEvent.create(activityId, participantId, participant.status());
        addDomainEvent(event);
        return event;
    }

    /**
     * 🎯 业务规则：取消参与活动
     */
    public DomainEvent cancelParticipation(UserId participantId) {
        var participant = findParticipant(participantId)
                .orElseThrow(() -> new IllegalArgumentException("用户未参与此活动"));

        if (!settings.canCancelNow(info.startTime())) {
            throw new IllegalStateException("已超过取消参与的截止时间");
        }

        // 取消参与
        var cancelledParticipant = participant.cancel();
        updateParticipant(cancelledParticipant);

        // 如果有候补列表，自动提升第一个候补者
        if (!waitingList.isEmpty()) {
            var waitingParticipant = waitingList.remove(0);
            var approvedParticipant = waitingParticipant.approve("候补自动通过");
            participants.add(approvedParticipant);

            // TODO: 添加候补列表提升事件
        }

        this.updateTime = LocalDateTime.now();

        // TODO: 添加取消参与事件
        return null; // 暂时返回null，后续实现具体事件
    }

    /**
     * 🎯 业务规则：审批参与申请
     */
    public DomainEvent approveParticipation(UserId participantId, String note) {
        validateIsOrganizer();

        var participant = findParticipant(participantId)
                .orElseThrow(() -> new IllegalArgumentException("未找到待审批的参与申请"));

        var approvedParticipant = participant.approve(note);
        updateParticipant(approvedParticipant);
        this.updateTime = LocalDateTime.now();

        // TODO: 添加审批通过事件
        return null;
    }

    /**
     * 🎯 业务规则：拒绝参与申请
     */
    public DomainEvent rejectParticipation(UserId participantId, String note) {
        validateIsOrganizer();

        var participant = findParticipant(participantId)
                .orElseThrow(() -> new IllegalArgumentException("未找到待审批的参与申请"));

        var rejectedParticipant = participant.reject(note);
        updateParticipant(rejectedParticipant);
        this.updateTime = LocalDateTime.now();

        // TODO: 添加审批拒绝事件
        return null;
    }

    /**
     * 🎯 业务规则：开始活动
     */
    public DomainEvent startActivity() {
        validateIsOrganizer();
        validateCanStart();

        this.status = ActivityStatus.ONGOING;
        this.updateTime = LocalDateTime.now();

        // TODO: 添加活动开始事件
        return null;
    }

    /**
     * 🎯 业务规则：结束活动
     */
    public DomainEvent finishActivity() {
        validateIsOrganizer();
        validateCanFinish();

        this.status = ActivityStatus.FINISHED;
        this.updateTime = LocalDateTime.now();

        // TODO: 添加活动结束事件
        return null;
    }

    /**
     * 🎯 业务规则：取消活动
     */
    public DomainEvent cancelActivity(String reason) {
        validateIsOrganizer();
        validateCanCancel();

        this.status = ActivityStatus.CANCELLED;
        this.updateTime = LocalDateTime.now();

        // TODO: 添加活动取消事件
        return null;
    }

    /**
     * 🎯 业务规则：更新活动信息
     */
    public void updateActivityInfo(ActivityInfo newInfo) {
        validateIsOrganizer();
        validateCanEdit();

        this.info = newInfo;
        this.updateTime = LocalDateTime.now();

        // TODO: 添加活动更新事件
    }

    /**
     * 🎯 业务规则：更新活动设置
     */
    public void updateActivitySettings(ActivitySettings newSettings) {
        validateIsOrganizer();
        validateCanEdit();

        this.settings = newSettings;
        this.updateTime = LocalDateTime.now();

        // TODO: 添加活动设置更新事件
    }

    // ========================================
    // 查询方法
    // ========================================

    /**
     * 获取已通过的参与者数量
     */
    public int getApprovedParticipantCount() {
        return (int) participants.stream()
                .filter(p -> p.status() == ActivityParticipant.ParticipantStatus.APPROVED)
                .count();
    }

    /**
     * 获取待审批的参与者数量
     */
    public int getPendingParticipantCount() {
        return (int) participants.stream()
                .filter(p -> p.status() == ActivityParticipant.ParticipantStatus.PENDING)
                .count();
    }

    /**
     * 获取候补列表大小
     */
    public int getWaitingListSize() {
        return waitingList.size();
    }

    /**
     * 检查用户是否已参与活动
     */
    public boolean isParticipant(UserId userId) {
        return participants.stream()
                .anyMatch(p -> p.userId().equals(userId) &&
                        p.status() == ActivityParticipant.ParticipantStatus.APPROVED);
    }

    /**
     * 检查用户是否在候补列表中
     */
    public boolean isInWaitingList(UserId userId) {
        return waitingList.stream()
                .anyMatch(p -> p.userId().equals(userId));
    }

    /**
     * 获取活动剩余名额
     */
    public int getRemainingSlots() {
        return Math.max(0, info.maxParticipants() - getApprovedParticipantCount());
    }

    /**
     * 检查活动是否已满
     */
    public boolean isFull() {
        return getRemainingSlots() == 0;
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    private void validateCanPublish() {
        if (status != ActivityStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的活动才能发布");
        }

        if (info.startTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalStateException("活动开始时间至少要在1小时后");
        }
    }

    private void validateCanJoin(UserId participantId) {
        if (!status.isJoinable()) {
            throw new IllegalStateException("当前活动状态不允许参与");
        }

        if (organizerId.equals(participantId)) {
            throw new IllegalArgumentException("组织者不能参与自己的活动");
        }

        if (isParticipant(participantId) || isInWaitingList(participantId)) {
            throw new IllegalArgumentException("用户已经参与或在候补列表中");
        }

        if (info.hasStarted()) {
            throw new IllegalStateException("活动已经开始，不能参与");
        }
    }

    private void validateCanStart() {
        if (status != ActivityStatus.PUBLISHED && status != ActivityStatus.REGISTERING) {
            throw new IllegalStateException("活动必须是已发布或报名中状态才能开始");
        }

        if (getApprovedParticipantCount() == 0) {
            throw new IllegalStateException("没有参与者的活动不能开始");
        }
    }

    private void validateCanFinish() {
        if (status != ActivityStatus.ONGOING) {
            throw new IllegalStateException("只有进行中的活动才能结束");
        }
    }

    private void validateCanCancel() {
        if (!status.isCancellable()) {
            throw new IllegalStateException("当前状态的活动不能取消");
        }
    }

    private void validateCanEdit() {
        if (!status.isEditable()) {
            throw new IllegalStateException("当前状态的活动不能编辑");
        }
    }

    private void validateIsOrganizer() {
        // 这里需要从安全上下文获取当前用户，简化处理
        // SecurityContextHolder.getContext().getAuthentication().getName()
        // 实际实现中应该验证当前操作用户是否为组织者
    }

    private Optional<ActivityParticipant> findParticipant(UserId userId) {
        return participants.stream()
                .filter(p -> p.userId().equals(userId))
                .findFirst();
    }

    private void updateParticipant(ActivityParticipant updatedParticipant) {
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).participantId().equals(updatedParticipant.participantId())) {
                participants.set(i, updatedParticipant);
                break;
            }
        }
    }

    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    // ========================================
    // Getter方法
    // ========================================

    public ActivityId getActivityId() {
        return activityId;
    }

    public UserId getOrganizerId() {
        return organizerId;
    }

    public ActivityInfo getInfo() {
        return info;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public ActivitySettings getSettings() {
        return settings;
    }

    public List<ActivityParticipant> getParticipants() {
        return List.copyOf(participants);
    }

    public List<ActivityParticipant> getWaitingList() {
        return List.copyOf(waitingList);
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

}
