package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.record.UserQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 *
 * @author XyPai
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    Optional<User> selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    Optional<User> selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     */
    Optional<User> selectByPhone(@Param("phone") String phone);

    /**
     * 分页查询用户列表
     */
    IPage<User> selectUserPage(Page<User> page, @Param("query") UserQueryRequest query);

    /**
     * 根据部门ID查询用户列表
     */
    List<User> selectByDeptId(@Param("deptId") Long deptId);

    /**
     * 根据状态查询用户列表
     */
    List<User> selectByStatus(@Param("status") Integer status);

    /**
     * 查询用户总数
     */
    Long selectUserCount();

    /**
     * 查询活跃用户总数
     */
    Long selectActiveUserCount();

    /**
     * 根据用户ID列表查询用户
     */
    List<User> selectByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 更新用户最后登录信息
     */
    int updateLoginInfo(@Param("userId") Long userId,
                        @Param("loginIp") String loginIp);

    /**
     * 批量更新用户状态
     */
    int updateUserStatus(@Param("userIds") List<Long> userIds,
                         @Param("status") Integer status);

    /**
     * 检查用户名是否已存在（排除指定用户ID）
     */
    boolean existsUsername(@Param("username") String username,
                           @Param("excludeUserId") Long excludeUserId);

    /**
     * 检查邮箱是否已存在（排除指定用户ID）
     */
    boolean existsEmail(@Param("email") String email,
                        @Param("excludeUserId") Long excludeUserId);

    /**
     * 检查手机号是否已存在（排除指定用户ID）
     */
    boolean existsPhone(@Param("phone") String phone,
                        @Param("excludeUserId") Long excludeUserId);
}
