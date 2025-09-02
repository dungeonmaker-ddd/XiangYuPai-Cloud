package com.xypai.content.service;

import com.xypai.content.domain.dto.ContentActionDTO;
import com.xypai.content.domain.dto.ContentActionQueryDTO;
import com.xypai.content.domain.vo.ContentActionVO;

import java.util.List;
import java.util.Map;

/**
 * 内容行为服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IContentActionService {

    /**
     * 执行内容行为(点赞、评论、分享等)
     *
     * @param actionDTO 行为数据
     * @return 是否成功
     */
    boolean executeAction(ContentActionDTO actionDTO);

    /**
     * 取消内容行为(取消点赞、删除评论等)
     *
     * @param contentId 内容ID
     * @param action 行为类型
     * @return 是否成功
     */
    boolean cancelAction(Long contentId, Integer action);

    /**
     * 点赞内容
     *
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean likeContent(Long contentId);

    /**
     * 取消点赞
     *
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean unlikeContent(Long contentId);

    /**
     * 收藏内容
     *
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean collectContent(Long contentId);

    /**
     * 取消收藏
     *
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean uncollectContent(Long contentId);

    /**
     * 评论内容
     *
     * @param contentId 内容ID
     * @param commentContent 评论内容
     * @param replyToId 回复的评论ID(可选)
     * @return 是否成功
     */
    boolean commentContent(Long contentId, String commentContent, Long replyToId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId);

    /**
     * 分享内容
     *
     * @param contentId 内容ID
     * @param shareTarget 分享目标(朋友圈、微信等)
     * @return 是否成功
     */
    boolean shareContent(Long contentId, String shareTarget);

    /**
     * 报名活动
     *
     * @param contentId 活动内容ID
     * @param signupInfo 报名信息
     * @return 是否成功
     */
    boolean signupActivity(Long contentId, Map<String, Object> signupInfo);

    /**
     * 取消报名
     *
     * @param contentId 活动内容ID
     * @return 是否成功
     */
    boolean cancelSignup(Long contentId);

    /**
     * 举报内容
     *
     * @param contentId 内容ID
     * @param reason 举报原因
     * @return 是否成功
     */
    boolean reportContent(Long contentId, String reason);

    /**
     * 查询内容行为列表
     *
     * @param query 查询条件
     * @return 行为列表
     */
    List<ContentActionVO> selectActionList(ContentActionQueryDTO query);

    /**
     * 查询内容的评论列表
     *
     * @param contentId 内容ID
     * @param limit 限制数量
     * @return 评论列表
     */
    List<ContentActionVO> selectContentComments(Long contentId, Integer limit);

    /**
     * 查询评论的回复列表
     *
     * @param commentId 评论ID
     * @return 回复列表
     */
    List<ContentActionVO> selectCommentReplies(Long commentId);

    /**
     * 查询用户的收藏列表
     *
     * @param userId 用户ID(可选，不传则查询当前用户)
     * @param limit 限制数量
     * @return 收藏列表
     */
    List<ContentActionVO> selectUserCollections(Long userId, Integer limit);

    /**
     * 查询活动报名列表
     *
     * @param contentId 活动内容ID
     * @return 报名列表
     */
    List<ContentActionVO> selectActivitySignups(Long contentId);

    /**
     * 获取内容行为统计
     *
     * @param contentId 内容ID
     * @return 统计信息
     */
    Map<String, Object> getContentActionStats(Long contentId);

    /**
     * 获取用户对内容的行为状态
     *
     * @param contentId 内容ID
     * @param userId 用户ID(可选，不传则查询当前用户)
     * @return 行为状态
     */
    Map<String, Object> getUserActionStatus(Long contentId, Long userId);

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean likeComment(Long commentId);

    /**
     * 取消评论点赞
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean unlikeComment(Long commentId);

    /**
     * 查询用户的动态(我的点赞、评论等行为记录)
     *
     * @param userId 用户ID(可选，不传则查询当前用户)
     * @param limit 限制数量
     * @return 动态列表
     */
    List<ContentActionVO> selectUserActivities(Long userId, Integer limit);

    /**
     * 查询热门评论
     *
     * @param contentId 内容ID
     * @param limit 限制数量
     * @return 热门评论列表
     */
    List<ContentActionVO> selectHotComments(Long contentId, Integer limit);
}
