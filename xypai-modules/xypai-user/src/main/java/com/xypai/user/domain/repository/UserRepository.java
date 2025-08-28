package com.xypai.user.domain.repository;

import com.xypai.user.domain.aggregate.UserAggregate;
import com.xypai.user.domain.valueobject.UserId;

import java.util.Optional;

/**
 * 👤 用户仓储接口 - 领域层
 *
 * @author XyPai
 * @since 2025-01-02
 */
public interface UserRepository {

    /**
     * 保存用户聚合根
     */
    UserAggregate save(UserAggregate userAggregate);

    /**
     * 根据用户ID查找
     */
    Optional<UserAggregate> findById(UserId userId);

    /**
     * 根据手机号查找
     */
    Optional<UserAggregate> findByMobile(String mobile);

    /**
     * 根据用户名查找
     */
    Optional<UserAggregate> findByUsername(String username);

    /**
     * 检查手机号是否存在
     */
    boolean existsByMobile(String mobile);

    /**
     * 检查用户名是否存在（排除指定用户ID）
     */
    boolean existsByUsername(String username, UserId excludeUserId);

    /**
     * 删除用户
     */
    void delete(UserId userId);
}
