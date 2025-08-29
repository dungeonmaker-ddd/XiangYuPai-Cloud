package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 👤 用户Mapper - MVP版本
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // MyBatis-Plus 提供了基础的CRUD操作
    // 如需自定义SQL，可在此添加方法
}
