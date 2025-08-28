package com.xypai.user.domain.repository;

import com.xypai.user.domain.aggregate.InteractionAggregate;
import com.xypai.user.domain.enums.TargetType;
import com.xypai.user.domain.valueobject.InteractionId;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 互动仓储接口 - 领域层
 *
 * @author XyPai
 * @since 2025-01-02
 */
public interface InteractionRepository {

    /**
     * 保存互动聚合根
     */
    InteractionAggregate save(InteractionAggregate interactionAggregate);

    /**
     * 根据互动ID查找
     */
    Optional<InteractionAggregate> findById(InteractionId interactionId);

    /**
     * 根据目标ID查找
     */
    Optional<InteractionAggregate> findByTargetId(TargetId targetId);

    /**
     * 根据目标ID和类型查找
     */
    Optional<InteractionAggregate> findByTargetIdAndType(TargetId targetId, TargetType targetType);

    /**
     * 批量根据目标ID查找
     */
    List<InteractionAggregate> findByTargetIds(List<TargetId> targetIds);

    /**
     * 检查用户是否点赞了目标
     */
    boolean isLikedByUser(TargetId targetId, UserId userId);

    /**
     * 检查用户是否收藏了目标
     */
    boolean isFavoritedByUser(TargetId targetId, UserId userId);

    /**
     * 获取用户的收藏列表
     */
    List<TargetId> findFavoritesByUserId(UserId userId);

    /**
     * 获取用户的点赞列表
     */
    List<TargetId> findLikesByUserId(UserId userId);

    /**
     * 根据目标类型获取热门内容（按点赞数排序）
     */
    List<TargetId> findPopularTargetsByType(TargetType targetType, int limit);

    /**
     * 删除互动聚合根
     */
    void delete(InteractionId interactionId);

    /**
     * 根据目标ID删除互动聚合根
     */
    void deleteByTargetId(TargetId targetId);
}
