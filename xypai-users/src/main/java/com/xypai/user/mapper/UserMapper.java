package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 🏗️ 用户数据访问层 - 企业架构实现 (MP QueryWrapper版本)
 * <p>
 * 遵循企业微服务架构规范：
 * - 继承MyBatis Plus BaseMapper，获得完整CRUD功能
 * - 使用QueryWrapper和LambdaQueryWrapper进行条件查询
 * - 只保留复杂的原生SQL查询
 * - 大幅简化Mapper接口和XML映射
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // ================================
    // 🔄 批量操作方法 (保留XML实现)
    // ================================

    /**
     * 批量更新用户状态
     * 注意：批量更新操作建议保留XML实现，性能更好
     *
     * @param userIds    用户ID列表
     * @param newStatus  新状态
     * @param operatorId 操作员ID
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE xypai_users " +
            "SET status = #{newStatus}, update_by = #{operatorId}, update_time = NOW() " +
            "WHERE user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' close=')' separator=','>" +
            "#{userId}" +
            "</foreach>" +
            "AND del_flag = '0'" +
            "</script>")
    int batchUpdateStatus(@Param("userIds") List<Long> userIds,
                          @Param("newStatus") Integer newStatus,
                          @Param("operatorId") String operatorId);

    /**
     * 更新登录信息
     *
     * @param userId  用户ID
     * @param loginIp 登录IP
     * @return 更新数量
     */
    @Update("UPDATE xypai_users " +
            "SET last_login_time = NOW(), last_login_ip = #{loginIp}, login_count = login_count + 1 " +
            "WHERE user_id = #{userId} AND del_flag = '0'")
    int updateLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);

    // ================================
    // 📊 复杂统计查询方法 (保留部分XML实现)
    // ================================

    /**
     * 按用户类型统计 - 使用原生SQL进行复杂统计
     *
     * @return 用户类型分布统计
     */
    @Select("SELECT " +
            "user_type AS type, " +
            "COUNT(*) AS count, " +
            "CASE user_type " +
            "WHEN 0 THEN '普通用户' " +
            "WHEN 1 THEN 'VIP用户' " +
            "WHEN 2 THEN 'SVIP用户' " +
            "WHEN 3 THEN '企业用户' " +
            "ELSE '未知' " +
            "END AS typeName " +
            "FROM xypai_users " +
            "WHERE del_flag = '0' AND status = 1 " +
            "GROUP BY user_type " +
            "ORDER BY user_type")
    List<Map<String, Object>> countByUserType();

    /**
     * 按平台统计
     *
     * @return 平台分布统计
     */
    @Select("SELECT platform, COUNT(*) AS count " +
            "FROM xypai_users " +
            "WHERE del_flag = '0' AND platform IS NOT NULL " +
            "GROUP BY platform " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countByPlatform();

    /**
     * 按注册渠道统计
     *
     * @return 渠道分布统计
     */
    @Select("SELECT source_channel AS channel, COUNT(*) AS count " +
            "FROM xypai_users " +
            "WHERE del_flag = '0' AND source_channel IS NOT NULL " +
            "GROUP BY source_channel " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countBySourceChannel();

    /**
     * 地区分布TOP统计
     *
     * @param limit TOP数量
     * @return 地区分布统计
     */
    @Select("SELECT location, COUNT(*) AS count " +
            "FROM xypai_users " +
            "WHERE del_flag = '0' AND location IS NOT NULL AND location != '' " +
            "GROUP BY location " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopLocationStats(@Param("limit") Integer limit);

    /**
     * 用户活跃度统计 - 复杂统计查询
     *
     * @return 活跃度统计数据
     */
    @Select("SELECT " +
            "COUNT(*) AS totalUsers, " +
            "COUNT(CASE WHEN last_login_time >= DATE_SUB(NOW(), INTERVAL 1 DAY) THEN 1 END) AS activeToday, " +
            "COUNT(CASE WHEN last_login_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) AS activeWeek, " +
            "COUNT(CASE WHEN last_login_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) AS activeMonth, " +
            "COUNT(CASE WHEN create_time >= DATE_SUB(NOW(), INTERVAL 1 DAY) THEN 1 END) AS newToday, " +
            "COUNT(CASE WHEN create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) AS newWeek, " +
            "COUNT(CASE WHEN create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) AS newMonth " +
            "FROM xypai_users " +
            "WHERE del_flag = '0' AND status = 1")
    Map<String, Object> getUserActivityStats();

    /**
     * 生成下一个用户编码 - 复杂业务逻辑
     *
     * @return 用户编码
     */
    @Select("SELECT CONCAT('XY', DATE_FORMAT(NOW(), '%Y%m%d'), " +
            "LPAD(IFNULL(MAX(SUBSTRING(user_code, -4)), 0) + 1, 4, '0')) " +
            "FROM xypai_users " +
            "WHERE user_code LIKE CONCAT('XY', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    String generateNextUserCode();

    /**
     * 查询用户注册趋势
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 注册趋势数据
     */
    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count " +
            "FROM xypai_users " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} AND del_flag = '0' " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getUserRegistrationTrend(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);
}
