package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.AppUser;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * APP用户数据访问层
 *
 * @author XyPai
 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {

    /**
     * 根据手机号查询用户（排除已删除）
     */
    @Select("SELECT * FROM app_user WHERE mobile = #{mobile} AND status = 1 AND deleted = 0")
    Optional<AppUser> findByMobile(@Param("mobile") String mobile);

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
    List<AppUser> findByStatus(@Param("status") Integer status);

    /**
     * 根据客户端类型查询用户列表（排除已删除）
     */
    @Select("SELECT * FROM app_user WHERE client_type = #{clientType} AND deleted = 0 ORDER BY register_time DESC")
    List<AppUser> findByClientType(@Param("clientType") String clientType);

    /**
     * 获取活跃用户数（排除已删除）
     */
    @Select("SELECT COUNT(1) FROM app_user WHERE status = 1 AND deleted = 0")
    Long countActiveUsers();

    /**
     * 获取禁用用户数（排除已删除）
     */
    @Select("SELECT COUNT(1) FROM app_user WHERE status = 0 AND deleted = 0")
    Long countDisabledUsers();

    // ========================================
    // 软删除相关方法
    // ========================================

    /**
     * 软删除用户
     */
    @Update("UPDATE app_user SET deleted = 1, delete_time = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int softDeleteById(@Param("userId") Long userId);

    /**
     * 恢复已删除用户
     */
    @Update("UPDATE app_user SET deleted = 0, delete_time = NULL WHERE user_id = #{userId} AND deleted = 1")
    int restoreById(@Param("userId") Long userId);

    /**
     * 查询已删除用户列表
     */
    @Select("SELECT * FROM app_user WHERE deleted = 1 ORDER BY delete_time DESC")
    List<AppUser> findDeletedUsers();

    /**
     * 查询已删除用户（分页）
     */
    @Select("SELECT * FROM app_user WHERE deleted = 1 ORDER BY delete_time DESC LIMIT #{offset}, #{size}")
    List<AppUser> findDeletedUsersWithPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 统计已删除用户数量
     */
    @Select("SELECT COUNT(1) FROM app_user WHERE deleted = 1")
    Long countDeletedUsers();

    /**
     * 根据ID查询已删除用户
     */
    @Select("SELECT * FROM app_user WHERE user_id = #{userId} AND deleted = 1")
    Optional<AppUser> findDeletedById(@Param("userId") Long userId);

    /**
     * 物理删除用户（危险操作，谨慎使用）
     */
    @Delete("DELETE FROM app_user WHERE user_id = #{userId}")
    int physicalDeleteById(@Param("userId") Long userId);

    // ========================================
    // DDD聚合根支持方法
    // ========================================

    /**
     * 根据手机号查询用户（包含已删除）
     */
    @Select("SELECT * FROM app_user WHERE mobile = #{mobile}")
    AppUser selectByMobile(@Param("mobile") String mobile);

    /**
     * 根据用户名查询用户（包含已删除）
     */
    @Select("SELECT * FROM app_user WHERE username = #{username}")
    AppUser selectByUsername(@Param("username") String username);

    /**
     * 检查手机号是否存在（包含已删除）
     */
    @Select("SELECT COUNT(1) > 0 FROM app_user WHERE mobile = #{mobile}")
    boolean existsByMobile(@Param("mobile") String mobile);

    /**
     * 检查用户名是否存在（排除指定用户ID，包含已删除）
     */
    @Select("SELECT COUNT(1) > 0 FROM app_user WHERE username = #{username} " +
            "AND (#{excludeId} IS NULL OR user_id != #{excludeId})")
    boolean existsByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

}
