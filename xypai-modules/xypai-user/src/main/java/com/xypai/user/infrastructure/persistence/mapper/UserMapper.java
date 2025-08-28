package com.xypai.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.infrastructure.persistence.po.UserPO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 *
 * @author XyPai
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    /**
     * 根据手机号查询用户（排除已删除）
     */
    @Select("SELECT * FROM app_user WHERE mobile = #{mobile} AND status = 1 AND deleted = 0")
    Optional<UserPO> findByMobile(@Param("mobile") String mobile);

    /**
     * 检查手机号是否存在（排除已删除）
     */
    @Select("SELECT COUNT(1) FROM app_user WHERE mobile = #{mobile} AND deleted = 0")
    Long countByMobile(@Param("mobile") String mobile);

    /**
     * 检查用户名是否存在（排除指定用户ID和已删除）
     */
    @Select("SELECT COUNT(1) FROM app_user WHERE username = #{username} AND deleted = 0 " +
            "AND (#{excludeId} IS NULL OR user_id != #{excludeId})")
    Long countByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * 根据状态查询用户列表（排除已删除）
     */
    @Select("SELECT * FROM app_user WHERE status = #{status} AND deleted = 0 ORDER BY register_time DESC")
    List<UserPO> findByStatus(@Param("status") Integer status);

    /**
     * 根据客户端类型查询用户列表（排除已删除）
     */
    @Select("SELECT * FROM app_user WHERE client_type = #{clientType} AND deleted = 0 ORDER BY register_time DESC")
    List<UserPO> findByClientType(@Param("clientType") String clientType);

    /**
     * 获取活跃用户数（排除已删除）
     */
    @Select("SELECT COUNT(1) FROM app_user WHERE status = 1 AND deleted = 0")
    Long countActiveUsers();

    /**
     * 软删除用户
     */
    @Update("UPDATE app_user SET deleted = 1, delete_time = NOW() WHERE user_id = #{userId}")
    Integer softDeleteById(@Param("userId") Long userId);

    /**
     * 恢复已删除用户
     */
    @Update("UPDATE app_user SET deleted = 0, delete_time = NULL WHERE user_id = #{userId}")
    Integer restoreById(@Param("userId") Long userId);

    /**
     * 查询已删除用户
     */
    @Select("SELECT * FROM app_user WHERE deleted = 1 ORDER BY delete_time DESC")
    List<UserPO> findDeletedUsers();

    /**
     * 根据ID查询已删除用户
     */
    @Select("SELECT * FROM app_user WHERE user_id = #{userId} AND deleted = 1")
    Optional<UserPO> findDeletedById(@Param("userId") Long userId);

    /**
     * 获取已删除用户数量
     */
    @Select("SELECT COUNT(1) FROM app_user WHERE deleted = 1")
    Long countDeletedUsers();

    /**
     * 物理删除用户（危险操作）
     */
    @Delete("DELETE FROM app_user WHERE user_id = #{userId}")
    Integer physicalDeleteById(@Param("userId") Long userId);

    /**
     * 更新最后登录时间
     */
    @Update("UPDATE app_user SET last_login_time = NOW() WHERE user_id = #{userId}")
    Integer updateLastLoginTime(@Param("userId") Long userId);

    /**
     * 根据年龄范围查询用户
     */
    @Select("SELECT * FROM app_user WHERE deleted = 0 AND status = 1 " +
            "AND YEAR(CURDATE()) - YEAR(birth_date) BETWEEN #{minAge} AND #{maxAge} " +
            "ORDER BY register_time DESC")
    List<UserPO> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    /**
     * 根据注册时间范围查询用户
     */
    @Select("SELECT * FROM app_user WHERE deleted = 0 AND status = 1 " +
            "AND register_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY register_time DESC")
    List<UserPO> findByRegisterTimeRange(@Param("startTime") String startTime,
                                         @Param("endTime") String endTime);

    /**
     * 批量更新用户状态
     */
    @Update("UPDATE app_user SET status = #{status} WHERE user_id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>")
    Integer batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);
}
