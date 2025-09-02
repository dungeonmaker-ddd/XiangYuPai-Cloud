package com.xypai.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.content.domain.dto.ContentAddDTO;
import com.xypai.content.domain.dto.ContentQueryDTO;
import com.xypai.content.domain.dto.ContentUpdateDTO;
import com.xypai.content.domain.entity.Content;
import com.xypai.content.domain.vo.ContentDetailVO;
import com.xypai.content.domain.vo.ContentListVO;
import com.xypai.content.mapper.ContentActionMapper;
import com.xypai.content.mapper.ContentMapper;
import com.xypai.content.service.IContentActionService;
import com.xypai.content.service.IContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容服务实现类
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements IContentService {

    private final ContentMapper contentMapper;
    private final ContentActionMapper contentActionMapper;
    private final IContentActionService contentActionService;

    @Override
    public List<ContentListVO> selectContentList(ContentQueryDTO query) {
        LambdaQueryWrapper<Content> queryWrapper = buildQueryWrapper(query);
        
        // 根据排序方式构建查询
        if ("popular".equals(query.getOrderBy())) {
            // 热门排序逻辑 - 这里简化为按创建时间排序
            queryWrapper.orderByDesc(Content::getCreatedAt);
        } else if ("recommended".equals(query.getOrderBy())) {
            // 推荐排序逻辑 - 这里简化为按创建时间排序
            queryWrapper.orderByDesc(Content::getCreatedAt);
        } else {
            // 默认最新排序
            queryWrapper.orderByDesc(Content::getCreatedAt);
        }

        List<Content> contents = contentMapper.selectList(queryWrapper);
        return convertToListVOs(contents);
    }

    @Override
    public ContentDetailVO selectContentById(Long contentId) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new ServiceException("内容不存在");
        }

        // 增加查看数(异步处理)
        incrementViewCount(contentId);

        return convertToDetailVO(content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createContent(ContentAddDTO contentAddDTO) {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 构建内容数据
        Map<String, Object> data = buildContentData(contentAddDTO);

        Content content = Content.builder()
                .userId(currentUserId)
                .type(contentAddDTO.getType())
                .title(contentAddDTO.getTitle())
                .data(data)
                .status(contentAddDTO.getPublish() ? Content.Status.PUBLISHED.getCode() : Content.Status.DRAFT.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = contentMapper.insert(content);
        if (result <= 0) {
            throw new ServiceException("创建内容失败");
        }

        log.info("创建内容成功，内容ID：{}，用户ID：{}", content.getId(), currentUserId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContent(ContentUpdateDTO contentUpdateDTO) {
        if (contentUpdateDTO.getId() == null) {
            throw new ServiceException("内容ID不能为空");
        }

        Content existContent = contentMapper.selectById(contentUpdateDTO.getId());
        if (existContent == null) {
            throw new ServiceException("内容不存在");
        }

        // 检查权限
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null && !currentUserId.equals(existContent.getUserId())) {
            throw new ServiceException("无权限修改该内容");
        }

        // 构建更新数据
        Map<String, Object> data = mergeContentData(existContent.getData(), contentUpdateDTO);

        Content updateContent = Content.builder()
                .id(contentUpdateDTO.getId())
                .title(contentUpdateDTO.getTitle())
                .data(data)
                .status(contentUpdateDTO.getStatus())
                .updatedAt(LocalDateTime.now())
                .version(contentUpdateDTO.getVersion())
                .build();

        int result = contentMapper.updateById(updateContent);
        if (result <= 0) {
            throw new ServiceException("更新内容失败");
        }

        log.info("更新内容成功，内容ID：{}", contentUpdateDTO.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteContent(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            throw new ServiceException("内容ID列表不能为空");
        }

        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 检查权限
        List<Content> contents = contentMapper.selectBatchIds(contentIds);
        for (Content content : contents) {
            if (!currentUserId.equals(content.getUserId())) {
                throw new ServiceException("无权限删除内容：" + content.getTitle());
            }
        }

        int result = contentMapper.deleteByIds(contentIds);
        log.info("删除内容成功，删除数量：{}", result);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishContent(Long contentId) {
        return updateContentStatus(contentId, Content.Status.PUBLISHED.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveContent(Long contentId, String reason) {
        boolean result = updateContentStatus(contentId, Content.Status.ARCHIVED.getCode());
        if (result) {
            log.info("下架内容成功，内容ID：{}，原因：{}", contentId, reason);
        }
        return result;
    }

    @Override
    public List<ContentListVO> selectMyContentList(ContentQueryDTO query) {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        query.setUserId(currentUserId);
        return selectContentList(query);
    }

    @Override
    public List<ContentListVO> selectFollowingContentList(ContentQueryDTO query) {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        List<Content> contents = contentMapper.selectFollowingContents(currentUserId);
        return convertToListVOs(contents);
    }

    @Override
    public List<ContentListVO> selectPopularContentList(Integer type, Integer limit) {
        List<Content> contents = contentMapper.selectPopularContents(limit != null ? limit : 20);
        
        if (type != null) {
            contents = contents.stream()
                    .filter(content -> type.equals(content.getType()))
                    .collect(Collectors.toList());
        }
        
        return convertToListVOs(contents);
    }

    @Override
    public List<ContentListVO> selectRecommendedContentList(Integer limit) {
        Long currentUserId = SecurityUtils.getUserId();
        List<Content> contents = contentMapper.selectRecommendedContents(
                currentUserId, limit != null ? limit : 20);
        
        return convertToListVOs(contents);
    }

    @Override
    public List<ContentListVO> searchContent(String keyword, Integer type, Integer limit) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Content> queryWrapper = Wrappers.lambdaQuery(Content.class)
                .like(Content::getTitle, keyword)
                .eq(type != null, Content::getType, type)
                .eq(Content::getStatus, Content.Status.PUBLISHED.getCode())
                .orderByDesc(Content::getCreatedAt)
                .last("LIMIT " + (limit != null ? limit : 20));

        List<Content> contents = contentMapper.selectList(queryWrapper);
        return convertToListVOs(contents);
    }

    @Override
    public Map<String, Object> getContentStatistics(Long contentId) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        return contentActionService.getContentActionStats(contentId);
    }

    @Override
    public Map<String, Object> getUserContentStatistics(Long userId) {
        Long targetUserId = userId != null ? userId : SecurityUtils.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        // 查询用户内容统计
        LambdaQueryWrapper<Content> queryWrapper = Wrappers.lambdaQuery(Content.class)
                .eq(Content::getUserId, targetUserId);

        List<Content> allContents = contentMapper.selectList(queryWrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", allContents.size());
        
        Map<Integer, Long> typeStats = allContents.stream()
                .collect(Collectors.groupingBy(Content::getType, Collectors.counting()));
        
        statistics.put("feedCount", typeStats.getOrDefault(Content.Type.FEED.getCode(), 0L));
        statistics.put("activityCount", typeStats.getOrDefault(Content.Type.ACTIVITY.getCode(), 0L));
        statistics.put("skillCount", typeStats.getOrDefault(Content.Type.SKILL.getCode(), 0L));
        
        Map<Integer, Long> statusStats = allContents.stream()
                .collect(Collectors.groupingBy(Content::getStatus, Collectors.counting()));
        
        statistics.put("publishedCount", statusStats.getOrDefault(Content.Status.PUBLISHED.getCode(), 0L));
        statistics.put("draftCount", statusStats.getOrDefault(Content.Status.DRAFT.getCode(), 0L));
        statistics.put("archivedCount", statusStats.getOrDefault(Content.Status.ARCHIVED.getCode(), 0L));

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyContent(Long sourceContentId, String newTitle) {
        if (sourceContentId == null) {
            throw new ServiceException("源内容ID不能为空");
        }

        Content sourceContent = contentMapper.selectById(sourceContentId);
        if (sourceContent == null) {
            throw new ServiceException("源内容不存在");
        }

        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        Content newContent = Content.builder()
                .userId(currentUserId)
                .type(sourceContent.getType())
                .title(StringUtils.isNotBlank(newTitle) ? newTitle : "复制 - " + sourceContent.getTitle())
                .data(new HashMap<>(sourceContent.getData()))
                .status(Content.Status.DRAFT.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = contentMapper.insert(newContent);
        log.info("复制内容成功，源内容ID：{}，新内容ID：{}", sourceContentId, newContent.getId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pinContent(Long contentId, Boolean pinned) {
        // TODO: 实现置顶逻辑，需要添加置顶字段到数据库
        log.info("{}内容，内容ID：{}", pinned ? "置顶" : "取消置顶", contentId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewContent(Long contentId, Boolean approved, String reason) {
        // TODO: 实现审核逻辑
        log.info("审核内容，内容ID：{}，结果：{}，原因：{}", contentId, approved ? "通过" : "拒绝", reason);
        return true;
    }

    @Override
    public List<ContentListVO> getRelatedContent(Long contentId, Integer limit) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            return new ArrayList<>();
        }

        List<Content> relatedContents = contentMapper.selectRelatedContents(
                contentId, content.getType(), limit != null ? limit : 5);
        
        return convertToListVOs(relatedContents);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementViewCount(Long contentId) {
        if (contentId == null) {
            return false;
        }

        try {
            contentMapper.incrementViewCount(contentId);
            return true;
        } catch (Exception e) {
            log.error("增加查看数失败，内容ID：{}", contentId, e);
            return false;
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Content> buildQueryWrapper(ContentQueryDTO query) {
        return Wrappers.lambdaQuery(Content.class)
                .eq(query.getUserId() != null, Content::getUserId, query.getUserId())
                .eq(query.getType() != null, Content::getType, query.getType())
                .eq(query.getStatus() != null, Content::getStatus, query.getStatus())
                .like(StringUtils.isNotBlank(query.getTitle()), Content::getTitle, query.getTitle())
                .between(StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime()),
                        Content::getCreatedAt, query.getBeginTime(), query.getEndTime());
    }

    /**
     * 构建内容数据
     */
    private Map<String, Object> buildContentData(ContentAddDTO dto) {
        Map<String, Object> data = new HashMap<>();
        
        // 基础信息
        if (StringUtils.isNotBlank(dto.getSummary())) {
            data.put("summary", dto.getSummary());
        }
        if (StringUtils.isNotBlank(dto.getContent())) {
            data.put("content", dto.getContent());
        }
        if (StringUtils.isNotBlank(dto.getCoverImage())) {
            data.put("cover_image", dto.getCoverImage());
        }
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            data.put("images", dto.getImages());
        }
        if (StringUtils.isNotBlank(dto.getVideoUrl())) {
            data.put("video_url", dto.getVideoUrl());
        }
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            data.put("tags", dto.getTags());
        }
        if (StringUtils.isNotBlank(dto.getLocation())) {
            data.put("location", dto.getLocation());
        }

        // 活动相关信息
        if (Content.Type.ACTIVITY.getCode().equals(dto.getType())) {
            if (StringUtils.isNotBlank(dto.getActivityStartTime())) {
                data.put("activity_start_time", dto.getActivityStartTime());
            }
            if (StringUtils.isNotBlank(dto.getActivityEndTime())) {
                data.put("activity_end_time", dto.getActivityEndTime());
            }
            if (StringUtils.isNotBlank(dto.getActivityLocation())) {
                data.put("activity_location", dto.getActivityLocation());
            }
            if (dto.getActivityMaxParticipants() != null) {
                data.put("activity_max_participants", dto.getActivityMaxParticipants());
            }
        }

        // 技能相关信息
        if (Content.Type.SKILL.getCode().equals(dto.getType())) {
            if (StringUtils.isNotBlank(dto.getSkillCategory())) {
                data.put("skill_category", dto.getSkillCategory());
            }
            if (StringUtils.isNotBlank(dto.getSkillLevel())) {
                data.put("skill_level", dto.getSkillLevel());
            }
            if (dto.getSkillPrice() != null) {
                data.put("skill_price", dto.getSkillPrice());
            }
        }

        // 扩展数据
        if (dto.getExtraData() != null) {
            data.putAll(dto.getExtraData());
        }

        return data;
    }

    /**
     * 合并内容数据
     */
    private Map<String, Object> mergeContentData(Map<String, Object> existData, ContentUpdateDTO dto) {
        Map<String, Object> data = existData != null ? new HashMap<>(existData) : new HashMap<>();
        
        // 更新基础信息
        if (StringUtils.isNotBlank(dto.getSummary())) {
            data.put("summary", dto.getSummary());
        }
        if (StringUtils.isNotBlank(dto.getContent())) {
            data.put("content", dto.getContent());
        }
        if (StringUtils.isNotBlank(dto.getCoverImage())) {
            data.put("cover_image", dto.getCoverImage());
        }
        if (dto.getImages() != null) {
            data.put("images", dto.getImages());
        }
        if (StringUtils.isNotBlank(dto.getVideoUrl())) {
            data.put("video_url", dto.getVideoUrl());
        }
        if (dto.getTags() != null) {
            data.put("tags", dto.getTags());
        }
        if (StringUtils.isNotBlank(dto.getLocation())) {
            data.put("location", dto.getLocation());
        }

        // 更新活动信息
        if (StringUtils.isNotBlank(dto.getActivityStartTime())) {
            data.put("activity_start_time", dto.getActivityStartTime());
        }
        if (StringUtils.isNotBlank(dto.getActivityEndTime())) {
            data.put("activity_end_time", dto.getActivityEndTime());
        }
        if (StringUtils.isNotBlank(dto.getActivityLocation())) {
            data.put("activity_location", dto.getActivityLocation());
        }
        if (dto.getActivityMaxParticipants() != null) {
            data.put("activity_max_participants", dto.getActivityMaxParticipants());
        }

        // 更新技能信息
        if (StringUtils.isNotBlank(dto.getSkillCategory())) {
            data.put("skill_category", dto.getSkillCategory());
        }
        if (StringUtils.isNotBlank(dto.getSkillLevel())) {
            data.put("skill_level", dto.getSkillLevel());
        }
        if (dto.getSkillPrice() != null) {
            data.put("skill_price", dto.getSkillPrice());
        }

        // 更新扩展数据
        if (dto.getExtraData() != null) {
            data.putAll(dto.getExtraData());
        }

        return data;
    }

    /**
     * 更新内容状态
     */
    private boolean updateContentStatus(Long contentId, Integer status) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new ServiceException("内容不存在");
        }

        // 检查权限
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null && !currentUserId.equals(content.getUserId())) {
            throw new ServiceException("无权限修改该内容状态");
        }

        Content updateContent = Content.builder()
                .id(contentId)
                .status(status)
                .updatedAt(LocalDateTime.now())
                .build();

        int result = contentMapper.updateById(updateContent);
        return result > 0;
    }

    /**
     * 转换为列表VO
     */
    private List<ContentListVO> convertToListVOs(List<Content> contents) {
        if (contents == null || contents.isEmpty()) {
            return new ArrayList<>();
        }

        List<ContentListVO> result = new ArrayList<>();
        for (Content content : contents) {
            ContentListVO vo = convertToListVO(content);
            result.add(vo);
        }
        return result;
    }

    /**
     * 转换为列表VO
     */
    private ContentListVO convertToListVO(Content content) {
        Map<String, Object> data = content.getData();
        
        ContentListVO vo = ContentListVO.builder()
                .id(content.getId())
                .userId(content.getUserId())
                .type(content.getType())
                .typeDesc(content.getTypeDesc())
                .title(content.getTitle())
                .summary(getSummaryFromData(data))
                .coverImage(getCoverImageFromData(data))
                .images(getImagesFromData(data))
                .tags(getTagsFromData(data))
                .location(getLocationFromData(data))
                .status(content.getStatus())
                .statusDesc(content.getStatusDesc())
                .createdAt(content.getCreatedAt())
                .build();

        // 获取行为统计
        Map<String, Object> stats = contentActionService.getContentActionStats(content.getId());
        vo.setLikeCount((Long) stats.getOrDefault("likeCount", 0L));
        vo.setCommentCount((Long) stats.getOrDefault("commentCount", 0L));
        vo.setShareCount((Long) stats.getOrDefault("shareCount", 0L));
        vo.setCollectCount((Long) stats.getOrDefault("collectCount", 0L));
        vo.setViewCount((Long) stats.getOrDefault("viewCount", 0L));

        // 获取当前用户行为状态
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            Map<String, Object> userStatus = contentActionService.getUserActionStatus(content.getId(), currentUserId);
            vo.setLiked((Boolean) userStatus.getOrDefault("liked", false));
            vo.setCollected((Boolean) userStatus.getOrDefault("collected", false));
        }

        return vo;
    }

    /**
     * 转换为详情VO
     */
    private ContentDetailVO convertToDetailVO(Content content) {
        Map<String, Object> data = content.getData();
        
        return ContentDetailVO.builder()
                .id(content.getId())
                .userId(content.getUserId())
                .type(content.getType())
                .typeDesc(content.getTypeDesc())
                .title(content.getTitle())
                .summary(getSummaryFromData(data))
                .content(getContentFromData(data))
                .coverImage(getCoverImageFromData(data))
                .images(getImagesFromData(data))
                .videoUrl(getVideoUrlFromData(data))
                .tags(getTagsFromData(data))
                .location(getLocationFromData(data))
                .status(content.getStatus())
                .statusDesc(content.getStatusDesc())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .version(content.getVersion())
                .extraData(data)
                .build();
    }

    // 数据提取辅助方法
    private String getSummaryFromData(Map<String, Object> data) {
        return data != null ? (String) data.get("summary") : null;
    }

    private String getContentFromData(Map<String, Object> data) {
        return data != null ? (String) data.get("content") : null;
    }

    private String getCoverImageFromData(Map<String, Object> data) {
        return data != null ? (String) data.get("cover_image") : null;
    }

    @SuppressWarnings("unchecked")
    private List<String> getImagesFromData(Map<String, Object> data) {
        return data != null ? (List<String>) data.get("images") : null;
    }

    private String getVideoUrlFromData(Map<String, Object> data) {
        return data != null ? (String) data.get("video_url") : null;
    }

    @SuppressWarnings("unchecked")
    private List<String> getTagsFromData(Map<String, Object> data) {
        return data != null ? (List<String>) data.get("tags") : null;
    }

    private String getLocationFromData(Map<String, Object> data) {
        return data != null ? (String) data.get("location") : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertContent(ContentAddDTO contentAddDTO) {
        return createContent(contentAddDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteContentByIds(List<Long> contentIds) {
        return deleteContent(contentIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveContent(Long contentId) {
        return archiveContent(contentId, "手动下架");
    }

    @Override
    public List<ContentListVO> getHotContents(Integer type, Integer limit) {
        return selectPopularContentList(type, limit);
    }

    @Override
    public List<ContentListVO> getRecommendedContents(Integer type, Integer limit) {
        List<ContentListVO> contents = selectRecommendedContentList(limit);
        if (type != null) {
            return contents.stream()
                    .filter(content -> type.equals(content.getType()))
                    .collect(Collectors.toList());
        }
        return contents;
    }

    @Override
    public List<ContentListVO> searchContents(String keyword, Integer type) {
        return searchContent(keyword, type, 50);
    }

    @Override
    public List<ContentListVO> getUserContents(Long userId, Integer type) {
        ContentQueryDTO query = ContentQueryDTO.builder()
                .userId(userId)
                .type(type)
                .status(Content.Status.PUBLISHED.getCode())
                .orderBy("latest")
                .build();
        return selectContentList(query);
    }

    @Override
    public List<ContentListVO> getMyContents(Integer type, Integer status) {
        ContentQueryDTO query = ContentQueryDTO.builder()
                .type(type)
                .status(status)
                .orderBy("latest")
                .build();
        return selectMyContentList(query);
    }

    @Override
    public Map<String, Object> getContentStatistics(String beginTime, String endTime) {
        // 根据时间范围统计内容
        LambdaQueryWrapper<Content> queryWrapper = Wrappers.lambdaQuery(Content.class)
                .between(StringUtils.isNotBlank(beginTime) && StringUtils.isNotBlank(endTime),
                        Content::getCreatedAt, beginTime, endTime);

        List<Content> contents = contentMapper.selectList(queryWrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", contents.size());
        
        Map<Integer, Long> typeStats = contents.stream()
                .collect(Collectors.groupingBy(Content::getType, Collectors.counting()));
        
        statistics.put("feedCount", typeStats.getOrDefault(Content.Type.FEED.getCode(), 0L));
        statistics.put("activityCount", typeStats.getOrDefault(Content.Type.ACTIVITY.getCode(), 0L));
        statistics.put("skillCount", typeStats.getOrDefault(Content.Type.SKILL.getCode(), 0L));
        
        return statistics;
    }

    @Override
    public Map<String, Object> getContentTypeStatistics() {
        List<Content> allContents = contentMapper.selectList(null);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", allContents.size());
        
        Map<Integer, Long> typeStats = allContents.stream()
                .collect(Collectors.groupingBy(Content::getType, Collectors.counting()));
        
        statistics.put("feedCount", typeStats.getOrDefault(Content.Type.FEED.getCode(), 0L));
        statistics.put("activityCount", typeStats.getOrDefault(Content.Type.ACTIVITY.getCode(), 0L));
        statistics.put("skillCount", typeStats.getOrDefault(Content.Type.SKILL.getCode(), 0L));
        
        Map<Integer, Long> statusStats = allContents.stream()
                .collect(Collectors.groupingBy(Content::getStatus, Collectors.counting()));
        
        statistics.put("publishedCount", statusStats.getOrDefault(Content.Status.PUBLISHED.getCode(), 0L));
        statistics.put("draftCount", statusStats.getOrDefault(Content.Status.DRAFT.getCode(), 0L));
        statistics.put("archivedCount", statusStats.getOrDefault(Content.Status.ARCHIVED.getCode(), 0L));
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchPublishContents(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            throw new ServiceException("内容ID列表不能为空");
        }

        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 检查权限
        List<Content> contents = contentMapper.selectBatchIds(contentIds);
        for (Content content : contents) {
            if (!currentUserId.equals(content.getUserId())) {
                throw new ServiceException("无权限发布内容：" + content.getTitle());
            }
        }

        // 批量更新状态
        for (Long contentId : contentIds) {
            publishContent(contentId);
        }
        
        log.info("批量发布内容成功，数量：{}", contentIds.size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchArchiveContents(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            throw new ServiceException("内容ID列表不能为空");
        }

        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 检查权限
        List<Content> contents = contentMapper.selectBatchIds(contentIds);
        for (Content content : contents) {
            if (!currentUserId.equals(content.getUserId())) {
                throw new ServiceException("无权限下架内容：" + content.getTitle());
            }
        }

        // 批量下架
        for (Long contentId : contentIds) {
            archiveContent(contentId, "批量下架");
        }
        
        log.info("批量下架内容成功，数量：{}", contentIds.size());
        return true;
    }
}
