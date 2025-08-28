package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.SocialRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 社交关系Mapper
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Mapper
public interface SocialRelationMapper extends BaseMapper<SocialRelation> {

    /**
     * 根据用户ID和关系类型查询社交关系
     */
    @Select("SELECT * FROM social_relation WHERE user_id = #{userId} AND relation_type = #{relationType}")
    List<SocialRelation> findByUserIdAndRelationType(@Param("userId") Long userId, @Param("relationType") String relationType);

    /**
     * 根据目标用户ID和关系类型查询社交关系
     */
    @Select("SELECT * FROM social_relation WHERE target_user_id = #{targetUserId} AND relation_type = #{relationType}")
    List<SocialRelation> findByTargetUserIdAndRelationType(@Param("targetUserId") Long targetUserId, @Param("relationType") String relationType);

    /**
     * 检查两个用户间是否存在特定关系
     */
    @Select("SELECT COUNT(1) FROM social_relation WHERE user_id = #{userId} AND target_user_id = #{targetUserId} AND relation_type = #{relationType}")
    boolean existsByUserIdAndTargetUserIdAndRelationType(
            @Param("userId") Long userId,
            @Param("targetUserId") Long targetUserId,
            @Param("relationType") String relationType
    );

    /**
     * 获取用户的关注数量
     */
    @Select("SELECT COUNT(1) FROM social_relation WHERE user_id = #{userId} AND relation_type = 'FOLLOW'")
    int countFollowingsByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的粉丝数量
     */
    @Select("SELECT COUNT(1) FROM social_relation WHERE target_user_id = #{userId} AND relation_type = 'FOLLOW'")
    int countFollowersByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的好友数量
     */
    @Select("SELECT COUNT(1) FROM social_relation WHERE user_id = #{userId} AND relation_type = 'FRIEND'")
    int countFriendsByUserId(@Param("userId") Long userId);

    /**
     * 删除特定的社交关系
     */
    @Delete("DELETE FROM social_relation WHERE user_id = #{userId} AND target_user_id = #{targetUserId} AND relation_type = #{relationType}")
    int deleteByUserIdAndTargetUserIdAndRelationType(
            @Param("userId") Long userId,
            @Param("targetUserId") Long targetUserId,
            @Param("relationType") String relationType
    );

    /**
     * 批量查询用户的关注关系
     */
    @Select("SELECT target_user_id FROM social_relation WHERE user_id = #{userId} AND relation_type = 'FOLLOW'")
    List<Long> findFollowingUserIds(@Param("userId") Long userId);

    /**
     * 批量查询用户的粉丝关系
     */
    @Select("SELECT user_id FROM social_relation WHERE target_user_id = #{userId} AND relation_type = 'FOLLOW'")
    List<Long> findFollowerUserIds(@Param("userId") Long userId);

    /**
     * 获取相互关注的用户列表（好友）
     */
    @Select("""
                SELECT DISTINCT sr1.target_user_id 
                FROM social_relation sr1 
                INNER JOIN social_relation sr2 ON sr1.target_user_id = sr2.user_id 
                WHERE sr1.user_id = #{userId} 
                  AND sr2.target_user_id = #{userId} 
                  AND sr1.relation_type = 'FOLLOW' 
                  AND sr2.relation_type = 'FOLLOW'
            """)
    List<Long> findMutualFollowingUserIds(@Param("userId") Long userId);

    /**
     * 批量插入社交关系
     */
    @Insert("""
                <script>
                INSERT INTO social_relation (user_id, target_user_id, relation_type, create_time) VALUES
                <foreach collection="relations" item="relation" separator=",">
                    (#{relation.userId}, #{relation.targetUserId}, #{relation.relationType}, NOW())
                </foreach>
                </script>
            """)
    int batchInsert(@Param("relations") List<SocialRelation> relations);
}
