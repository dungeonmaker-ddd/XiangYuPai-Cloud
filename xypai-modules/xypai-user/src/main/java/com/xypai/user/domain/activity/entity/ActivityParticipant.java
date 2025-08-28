package com.xypai.user.domain.activity.entity;

import com.xypai.user.domain.user.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 活动参与者实体
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record ActivityParticipant(
        ParticipantId participantId,
        UserId userId,
        ParticipantStatus status,
        String joinReason,
        String specialRequirements,
        LocalDateTime joinTime,
        LocalDateTime approvalTime,
        String approvalNote
) {

    public ActivityParticipant {
        Objects.requireNonNull(participantId, "参与者ID不能为空");
        Objects.requireNonNull(userId, "用户ID不能为空");
        Objects.requireNonNull(status, "参与状态不能为空");
        Objects.requireNonNull(joinTime, "加入时间不能为空");

        // 业务规则验证
        if (joinReason != null && joinReason.length() > 500) {
            throw new IllegalArgumentException("参与理由不能超过500个字符");
        }
        if (specialRequirements != null && specialRequirements.length() > 500) {
            throw new IllegalArgumentException("特殊要求不能超过500个字符");
        }
    }

    /**
     * 静态工厂方法：创建参与者
     */
    public static ActivityParticipant create(UserId userId, String joinReason) {
        return new ActivityParticipant(
                ParticipantId.generate(),
                userId,
                ParticipantStatus.PENDING,
                joinReason,
                null, // specialRequirements
                LocalDateTime.now(),
                null, // approvalTime
                null // approvalNote
        );
    }

    /**
     * 静态工厂方法：创建参与者（无需审批）
     */
    public static ActivityParticipant createApproved(UserId userId, String joinReason) {
        return new ActivityParticipant(
                ParticipantId.generate(),
                userId,
                ParticipantStatus.APPROVED,
                joinReason,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "自动通过"
        );
    }

    /**
     * 审批通过
     */
    public ActivityParticipant approve(String note) {
        if (status != ParticipantStatus.PENDING) {
            throw new IllegalStateException("只有待审批状态的参与者才能审批通过");
        }

        return new ActivityParticipant(
                this.participantId,
                this.userId,
                ParticipantStatus.APPROVED,
                this.joinReason,
                this.specialRequirements,
                this.joinTime,
                LocalDateTime.now(),
                note
        );
    }

    /**
     * 审批拒绝
     */
    public ActivityParticipant reject(String note) {
        if (status != ParticipantStatus.PENDING) {
            throw new IllegalStateException("只有待审批状态的参与者才能审批拒绝");
        }

        return new ActivityParticipant(
                this.participantId,
                this.userId,
                ParticipantStatus.REJECTED,
                this.joinReason,
                this.specialRequirements,
                this.joinTime,
                LocalDateTime.now(),
                note
        );
    }

    /**
     * 取消参与
     */
    public ActivityParticipant cancel() {
        if (status == ParticipantStatus.CANCELLED) {
            throw new IllegalStateException("参与者已经取消参与");
        }

        return new ActivityParticipant(
                this.participantId,
                this.userId,
                ParticipantStatus.CANCELLED,
                this.joinReason,
                this.specialRequirements,
                this.joinTime,
                this.approvalTime,
                this.approvalNote
        );
    }

    /**
     * 检查是否可以参与活动
     */
    public boolean canParticipate() {
        return status == ParticipantStatus.APPROVED;
    }

    /**
     * 参与者状态枚举
     */
    public enum ParticipantStatus {
        /**
         * 待审批
         */
        PENDING("待审批"),

        /**
         * 已通过
         */
        APPROVED("已通过"),

        /**
         * 已拒绝
         */
        REJECTED("已拒绝"),

        /**
         * 已取消
         */
        CANCELLED("已取消");

        private final String description;

        ParticipantStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 参与者ID值对象
     */
    public record ParticipantId(String value) {
        public ParticipantId {
            Objects.requireNonNull(value, "参与者ID不能为空");
        }

        public static ParticipantId generate() {
            return new ParticipantId("participant_" + UUID.randomUUID().toString().replace("-", ""));
        }

        public static ParticipantId of(String value) {
            return new ParticipantId(value);
        }
    }
}
