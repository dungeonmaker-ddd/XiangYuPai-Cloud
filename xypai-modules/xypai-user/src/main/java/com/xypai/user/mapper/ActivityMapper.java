package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.ActivityInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 活动Mapper
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Mapper
public interface ActivityMapper extends BaseMapper<ActivityInfo> {

    /**
     * 根据组织者ID查询活动列表
     */
    @Select("SELECT * FROM activity WHERE organizer_id = #{organizerId} ORDER BY create_time DESC")
    List<ActivityInfo> findByOrganizerId(@Param("organizerId") Long organizerId);

    /**
     * 根据状态查询活动列表
     */
    @Select("SELECT * FROM activity WHERE status = #{status} ORDER BY start_time ASC")
    List<ActivityInfo> findByStatus(@Param("status") String status);

    /**
     * 根据活动类型查询活动列表
     */
    @Select("SELECT * FROM activity WHERE type = #{type} AND status = 'PUBLISHED' ORDER BY start_time ASC")
    List<ActivityInfo> findByType(@Param("type") String type);

    /**
     * 查询即将开始的活动
     */
    @Select("""
                SELECT * FROM activity 
                WHERE status = 'PUBLISHED' 
                  AND start_time > NOW() 
                  AND start_time <= DATE_ADD(NOW(), INTERVAL #{hours} HOUR)
                ORDER BY start_time ASC
            """)
    List<ActivityInfo> findUpcomingActivities(@Param("hours") int hours);

    /**
     * 查询正在进行的活动
     */
    @Select("""
                SELECT * FROM activity 
                WHERE status = 'ONGOING' 
                  OR (status = 'PUBLISHED' AND start_time <= NOW() AND end_time > NOW())
                ORDER BY start_time ASC
            """)
    List<ActivityInfo> findOngoingActivities();

    /**
     * 根据地理位置查询附近的活动
     */
    @Select("""
                SELECT * FROM activity 
                WHERE status = 'PUBLISHED' 
                  AND start_time > NOW()
                  AND location LIKE CONCAT('%', #{location}, '%')
                ORDER BY start_time ASC
            """)
    List<ActivityInfo> findByLocationContaining(@Param("location") String location);

    /**
     * 查询热门活动（按参与人数排序）
     */
    @Select("""
                SELECT a.*, COUNT(ap.user_id) as participant_count
                FROM activity a 
                LEFT JOIN activity_participant ap ON a.activity_id = ap.activity_id 
                    AND ap.status = 'APPROVED'
                WHERE a.status = 'PUBLISHED' AND a.start_time > NOW()
                GROUP BY a.activity_id
                ORDER BY participant_count DESC, a.create_time DESC
                LIMIT #{limit}
            """)
    List<ActivityInfo> findPopularActivities(@Param("limit") int limit);

    /**
     * 更新活动状态
     */
    @Update("UPDATE activity SET status = #{status}, update_time = NOW() WHERE activity_id = #{activityId}")
    int updateStatus(@Param("activityId") Long activityId, @Param("status") String status);

    /**
     * 统计用户创建的活动数量
     */
    @Select("SELECT COUNT(1) FROM activity WHERE organizer_id = #{organizerId}")
    int countByOrganizerId(@Param("organizerId") Long organizerId);

    /**
     * 查询用户参与的活动
     */
    @Select("""
                SELECT a.* FROM activity a 
                INNER JOIN activity_participant ap ON a.activity_id = ap.activity_id
                WHERE ap.user_id = #{userId} AND ap.status = #{participantStatus}
                ORDER BY a.start_time DESC
            """)
    List<ActivityInfo> findByParticipantUserId(@Param("userId") Long userId, @Param("participantStatus") String participantStatus);

    /**
     * 检查用户是否已报名参加活动
     */
    @Select("""
                SELECT COUNT(1) FROM activity_participant 
                WHERE activity_id = #{activityId} AND user_id = #{userId}
            """)
    boolean existsParticipant(@Param("activityId") Long activityId, @Param("userId") Long userId);

    /**
     * 获取活动的参与人数
     */
    @Select("""
                SELECT COUNT(1) FROM activity_participant 
                WHERE activity_id = #{activityId} AND status = 'APPROVED'
            """)
    int countApprovedParticipants(@Param("activityId") Long activityId);

    /**
     * 查询需要自动更新状态的活动
     */
    @Select("""
                SELECT * FROM activity 
                WHERE (status = 'PUBLISHED' AND start_time <= NOW() AND end_time > NOW())
                   OR (status = 'ONGOING' AND end_time <= NOW())
            """)
    List<ActivityInfo> findActivitiesNeedingStatusUpdate();
}
