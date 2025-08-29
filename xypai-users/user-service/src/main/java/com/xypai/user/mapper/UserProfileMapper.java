package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 👤 用户扩展信息Mapper - MVP版本
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    // MyBatis-Plus 提供了基础的CRUD操作
}
