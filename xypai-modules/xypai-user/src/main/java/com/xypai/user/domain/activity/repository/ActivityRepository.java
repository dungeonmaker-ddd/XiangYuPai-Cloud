package com.xypai.user.domain.activity.repository;

import com.xypai.user.domain.activity.ActivityAggregate;
import com.xypai.user.domain.activity.valueobject.ActivityId;
import com.xypai.user.domain.user.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 📱 活动仓储接口 - 活动聚合根持久化
 *
 * @author XyPai
 * @since 2025-01-02
 */
public interface ActivityRepository {

    /**
     * 💾 保存活动聚合根
     */
    ActivityAggregate save(ActivityAggregate activityAggregate);

    /**
     * 🔍 根据活动ID查找
     */
    Optional<ActivityAggregate> findById(ActivityId activityId);

    /**
     * 🔍 根据组织者ID查找活动列表
     */
    List<ActivityAggregate> findByOrganizerId(UserId organizerId);

    /**
     * 🔍 查找用户参与的活动列表
     */
    List<ActivityAggregate> findByParticipantId(UserId participantId);

    /**
     * 🗑️ 删除活动
     */
    void deleteById(ActivityId activityId);

    /**
     * ✅ 检查活动是否存在
     */
    boolean existsById(ActivityId activityId);
}
