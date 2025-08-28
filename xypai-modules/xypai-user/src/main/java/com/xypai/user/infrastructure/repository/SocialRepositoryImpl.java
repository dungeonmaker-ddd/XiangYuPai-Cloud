package com.xypai.user.infrastructure.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypai.user.domain.aggregate.SocialAggregate;
import com.xypai.user.domain.entity.FollowRelation;
import com.xypai.user.domain.entity.SocialSettings;
import com.xypai.user.domain.repository.SocialRepository;
import com.xypai.user.domain.valueobject.SocialId;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.mapper.SocialRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 社交仓储实现
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SocialRepositoryImpl implements SocialRepository {

    private final SocialRelationMapper socialRelationMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<SocialAggregate> findByUserId(UserId userId) {
        try {
            Long userIdValue = Long.parseLong(userId.value());

            // 查询关注关系
            var followingRelations = socialRelationMapper.findByUserIdAndRelationType(userIdValue, "FOLLOW")
                    .stream()
                    .map(this::convertToFollowRelation)
                    .collect(Collectors.toList());

            // 查询粉丝关系
            var followerRelations = socialRelationMapper.findByTargetUserIdAndRelationType(userIdValue, "FOLLOW")
                    .stream()
                    .map(this::convertToFollowRelation)
                    .collect(Collectors.toList());

            // 查询社交设置（如果不存在则使用默认设置）
            var socialSettings = SocialSettings.defaultSettings();

            // 重构聚合根
            var socialAggregate = SocialAggregate.reconstruct(
                    SocialId.of(userId.value()),
                    userId,
                    followingRelations,
                    followerRelations,
                    socialSettings
            );

            return Optional.of(socialAggregate);

        } catch (Exception e) {
            log.error("查询社交聚合根失败: userId={}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void save(SocialAggregate aggregate) {
        try {
            Long userIdValue = Long.parseLong(aggregate.getUserId().value());

            // 删除现有关注关系
            socialRelationMapper.deleteByUserIdAndTargetUserIdAndRelationType(userIdValue, null, "FOLLOW");

            // 保存关注关系
            var followingRelations = aggregate.getFollowings().stream()
                    .map(relation -> convertToSocialRelationEntity(userIdValue, relation, "FOLLOW"))
                    .collect(Collectors.toList());

            if (!followingRelations.isEmpty()) {
                socialRelationMapper.batchInsert(followingRelations);
            }

            log.info("保存社交聚合根成功: userId={}, followingCount={}",
                    aggregate.getUserId(), aggregate.getFollowingCount());

        } catch (Exception e) {
            log.error("保存社交聚合根失败: userId={}", aggregate.getUserId(), e);
            throw new RuntimeException("保存社交聚合根失败", e);
        }
    }

    @Override
    @Transactional
    public void delete(SocialAggregate aggregate) {
        try {
            Long userIdValue = Long.parseLong(aggregate.getUserId().value());

            // 删除所有相关的社交关系
            // 删除用户作为关注者的关系
            socialRelationMapper.findByUserIdAndRelationType(userIdValue, "FOLLOW")
                    .forEach(relation -> socialRelationMapper.deleteById(relation.getId()));

            // 删除用户作为被关注者的关系
            socialRelationMapper.findByTargetUserIdAndRelationType(userIdValue, "FOLLOW")
                    .forEach(relation -> socialRelationMapper.deleteById(relation.getId()));

            log.info("删除社交聚合根成功: userId={}", aggregate.getUserId());

        } catch (Exception e) {
            log.error("删除社交聚合根失败: userId={}", aggregate.getUserId(), e);
            throw new RuntimeException("删除社交聚合根失败", e);
        }
    }

    /**
     * 检查用户是否关注目标用户
     */
    public boolean isFollowing(UserId userId, UserId targetUserId) {
        try {
            Long userIdValue = Long.parseLong(userId.value());
            Long targetUserIdValue = Long.parseLong(targetUserId.value());

            return socialRelationMapper.existsByUserIdAndTargetUserIdAndRelationType(
                    userIdValue, targetUserIdValue, "FOLLOW"
            );

        } catch (Exception e) {
            log.error("检查关注关系失败: userId={}, targetUserId={}", userId, targetUserId, e);
            return false;
        }
    }

    /**
     * 获取用户的社交统计信息
     */
    public SocialStats getSocialStats(UserId userId) {
        try {
            Long userIdValue = Long.parseLong(userId.value());

            int followingCount = socialRelationMapper.countFollowingsByUserId(userIdValue);
            int followerCount = socialRelationMapper.countFollowersByUserId(userIdValue);
            int friendCount = socialRelationMapper.findMutualFollowingUserIds(userIdValue).size();

            return new SocialStats(followingCount, followerCount, friendCount);

        } catch (Exception e) {
            log.error("获取社交统计失败: userId={}", userId, e);
            return new SocialStats(0, 0, 0);
        }
    }

    /**
     * 转换为关注关系实体
     */
    private FollowRelation convertToFollowRelation(SocialRelationEntity relation) {
        return FollowRelation.create(
                UserId.of(relation.getUserId().toString()),
                UserId.of(relation.getTargetUserId().toString())
        );
    }

    /**
     * 转换为社交关系数据库实体
     */
    private SocialRelationEntity convertToSocialRelationEntity(
            Long userId, FollowRelation relation, String relationType) {

        var entity = new SocialRelationEntity();
        entity.setUserId(userId);
        entity.setTargetUserId(Long.parseLong(relation.targetUserId().value()));
        entity.setRelationType(relationType);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        return entity;
    }

    /**
     * 社交统计记录
     */
    public static record SocialStats(
            int followingCount,
            int followerCount,
            int friendCount
    ) {
    }

    /**
     * 社交关系数据库实体
     */
    public static class SocialRelationEntity {
        private Long id;
        private Long userId;
        private Long targetUserId;
        private String relationType;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getTargetUserId() {
            return targetUserId;
        }

        public void setTargetUserId(Long targetUserId) {
            this.targetUserId = targetUserId;
        }

        public String getRelationType() {
            return relationType;
        }

        public void setRelationType(String relationType) {
            this.relationType = relationType;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }
    }
}
