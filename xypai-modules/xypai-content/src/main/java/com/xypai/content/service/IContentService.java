package com.xypai.content.service;

import com.xypai.content.domain.dto.ContentAddDTO;
import com.xypai.content.domain.dto.ContentQueryDTO;
import com.xypai.content.domain.dto.ContentUpdateDTO;
import com.xypai.content.domain.vo.ContentDetailVO;
import com.xypai.content.domain.vo.ContentListVO;

import java.util.List;
import java.util.Map;

/**
 * 内容服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IContentService {

    /**
     * 查询内容列表
     *
     * @param query 查询条件
     * @return 内容列表
     */
    List<ContentListVO> selectContentList(ContentQueryDTO query);

    /**
     * 根据内容ID查询详情
     *
     * @param contentId 内容ID
     * @return 内容详情
     */
    ContentDetailVO selectContentById(Long contentId);

    /**
     * 创建内容
     *
     * @param contentAddDTO 内容数据
     * @return 是否成功
     */
    boolean createContent(ContentAddDTO contentAddDTO);

    /**
     * 更新内容
     *
     * @param contentUpdateDTO 更新数据
     * @return 是否成功
     */
    boolean updateContent(ContentUpdateDTO contentUpdateDTO);

    /**
     * 删除内容
     *
     * @param contentIds 内容ID列表
     * @return 是否成功
     */
    boolean deleteContent(List<Long> contentIds);

    /**
     * 发布内容(草稿 -> 发布)
     *
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean publishContent(Long contentId);

    /**
     * 下架内容(发布 -> 下架)
     *
     * @param contentId 内容ID
     * @param reason 下架原因
     * @return 是否成功
     */
    boolean archiveContent(Long contentId, String reason);

    /**
     * 查询我的内容列表
     *
     * @param query 查询条件
     * @return 内容列表
     */
    List<ContentListVO> selectMyContentList(ContentQueryDTO query);

    /**
     * 查询关注用户的内容(动态流)
     *
     * @param query 查询条件
     * @return 内容列表
     */
    List<ContentListVO> selectFollowingContentList(ContentQueryDTO query);

    /**
     * 查询热门内容
     *
     * @param type 内容类型(可选)
     * @param limit 限制数量
     * @return 内容列表
     */
    List<ContentListVO> selectPopularContentList(Integer type, Integer limit);

    /**
     * 查询推荐内容
     *
     * @param limit 限制数量
     * @return 内容列表
     */
    List<ContentListVO> selectRecommendedContentList(Integer limit);

    /**
     * 搜索内容
     *
     * @param keyword 关键词
     * @param type 内容类型(可选)
     * @param limit 限制数量
     * @return 内容列表
     */
    List<ContentListVO> searchContent(String keyword, Integer type, Integer limit);

    /**
     * 获取内容统计信息
     *
     * @param contentId 内容ID
     * @return 统计信息
     */
    Map<String, Object> getContentStatistics(Long contentId);

    /**
     * 获取用户内容统计
     *
     * @param userId 用户ID(可选，不传则查询当前用户)
     * @return 统计信息
     */
    Map<String, Object> getUserContentStatistics(Long userId);

    /**
     * 复制内容(基于现有内容创建新内容)
     *
     * @param sourceContentId 源内容ID
     * @param newTitle 新标题
     * @return 是否成功
     */
    boolean copyContent(Long sourceContentId, String newTitle);

    /**
     * 置顶内容(管理员功能)
     *
     * @param contentId 内容ID
     * @param pinned 是否置顶
     * @return 是否成功
     */
    boolean pinContent(Long contentId, Boolean pinned);

    /**
     * 审核内容(管理员功能)
     *
     * @param contentId 内容ID
     * @param approved 是否通过
     * @param reason 审核原因
     * @return 是否成功
     */
    boolean reviewContent(Long contentId, Boolean approved, String reason);

    /**
     * 获取相关内容推荐
     *
     * @param contentId 内容ID
     * @param limit 限制数量
     * @return 相关内容列表
     */
    List<ContentListVO> getRelatedContent(Long contentId, Integer limit);

    /**
     * 增加内容查看数
     *
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long contentId);
}
