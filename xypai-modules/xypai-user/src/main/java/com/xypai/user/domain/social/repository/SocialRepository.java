package com.xypai.user.domain.social.repository;

import com.xypai.user.domain.social.SocialAggregate;
import com.xypai.user.domain.social.valueobject.SocialId;
import com.xypai.user.domain.user.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 🤝 社交仓储接口 - 领域层
 *
 * @author XyPai
 * @since 2025-01-02
 */
public interface SocialRepository {

    /**
     * 保存社交聚合根
     */
    SocialAggregate save(SocialAggregate socialAggregate);

    /**
     * 根据社交ID查找
     */
    Optional<SocialAggregate> findById(SocialId socialId);

    /**
     * 根据用户ID查找
     */
    Optional<SocialAggregate> findByUserId(UserId userId);

    /**
     * 批量根据用户ID查找
     */
    List<SocialAggregate> findByUserIds(List<UserId> userIds);

    /**
     * 查找某用户的关注者（粉丝）
     */
    List<UserId> findFollowersByUserId(UserId userId);

    /**
     * 查找某用户关注的人
     */
    List<UserId> findFollowingsByUserId(UserId userId);

    /**
     * 检查用户A是否关注用户B
     */
    boolean isFollowing(UserId followerUserId, UserId followeeUserId);

    /**
     * 删除社交聚合根
     */
    void delete(SocialId socialId);

    /**
     * 根据用户ID删除社交聚合根
     */
    void deleteByUserId(UserId userId);
}
