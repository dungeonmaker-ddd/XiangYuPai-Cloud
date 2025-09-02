package com.xypai.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.content.domain.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {

    /**
     * 查询用户关注的内容列表
     *
     * @param userId 当前用户ID
     * @return 内容列表
     */
    List<Content> selectFollowingContents(@Param("userId") Long userId);

    /**
     * 查询热门内容列表
     *
     * @param limit 限制数量
     * @return 内容列表
     */
    List<Content> selectPopularContents(@Param("limit") Integer limit);

    /**
     * 查询推荐内容列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 内容列表
     */
    List<Content> selectRecommendedContents(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 增加查看数
     *
     * @param contentId 内容ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("contentId") Long contentId);

    /**
     * 查询相关内容
     *
     * @param contentId 内容ID
     * @param type 内容类型
     * @param limit 限制数量
     * @return 内容列表
     */
    List<Content> selectRelatedContents(@Param("contentId") Long contentId, 
                                       @Param("type") Integer type, 
                                       @Param("limit") Integer limit);
}
