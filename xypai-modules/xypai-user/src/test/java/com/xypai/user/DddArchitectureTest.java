package com.xypai.user;

import com.xypai.user.application.command.CreateUserCommand;
import com.xypai.user.application.command.FollowUserCommand;
import com.xypai.user.domain.aggregate.FeedAggregate;
import com.xypai.user.domain.aggregate.InteractionAggregate;
import com.xypai.user.domain.aggregate.SocialAggregate;
import com.xypai.user.domain.aggregate.UserAggregate;
import com.xypai.user.domain.entity.FeedContent;
import com.xypai.user.domain.entity.FeedSettings;
import com.xypai.user.domain.entity.FollowRelation;
import com.xypai.user.domain.enums.FeedStatus;
import com.xypai.user.domain.enums.FeedType;
import com.xypai.user.domain.enums.TargetType;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 🧪 DDD架构测试
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class DddArchitectureTest {

    @Test
    public void testUserAggregateCreation() {
        // 测试用户聚合根创建
        var userAggregate = UserAggregate.createUserForTest(
                "13888888888",
                "测试用户",
                "app"
        );

        assertNotNull(userAggregate);
        assertEquals("13888888888", userAggregate.getMobile());
        assertEquals("测试用户", userAggregate.getNickname());
        assertEquals("app", userAggregate.getClientType());
        assertTrue(userAggregate.isActive());
        assertFalse(userAggregate.getDomainEvents().isEmpty());
    }

    @Test
    public void testUserAggregateUpdateProfile() {
        // 测试用户聚合根更新信息
        var userAggregate = UserAggregate.createUserForTest(
                "13888888888",
                "测试用户",
                "app"
        );

        userAggregate.updateProfile(
                "testuser",
                "新昵称",
                "https://example.com/avatar.jpg",
                1,
                LocalDate.of(1990, 1, 1)
        );

        assertEquals("testuser", userAggregate.getUsername());
        assertEquals("新昵称", userAggregate.getNickname());
        assertEquals("https://example.com/avatar.jpg", userAggregate.getAvatar());
        assertEquals(1, userAggregate.getGender());
        assertEquals(LocalDate.of(1990, 1, 1), userAggregate.getBirthDate());
    }

    @Test
    public void testUserAggregateBusinessRules() {
        // 测试业务规则验证
        assertThrows(IllegalArgumentException.class, () -> {
            UserAggregate.createUserForTest("invalid_mobile", "测试用户", "app");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            UserAggregate.createUserForTest("13888888888", "", "app");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            UserAggregate.createUserForTest("13888888888", "测试用户", "invalid_client");
        });
    }

    @Test
    public void testSocialAggregateCreation() {
        // 测试社交聚合根创建
        var userId = UserId.of(1L);
        var socialAggregate = SocialAggregate.create(userId);

        assertNotNull(socialAggregate);
        assertEquals(userId, socialAggregate.getUserId());
        assertEquals(0, socialAggregate.getFollowingCount());
        assertEquals(0, socialAggregate.getFollowerCount());
    }

    @Test
    public void testSocialAggregateFollowUser() {
        // 测试关注用户
        var userId = UserId.of(1L);
        var targetUserId = UserId.of(2L);
        var socialAggregate = SocialAggregate.create(userId);

        var event = socialAggregate.followUser(targetUserId);

        assertNotNull(event);
        assertEquals("user.followed", event.eventType());
        assertTrue(socialAggregate.isFollowing(targetUserId));
        assertEquals(1, socialAggregate.getFollowingCount());
        assertFalse(socialAggregate.getDomainEvents().isEmpty());
    }

    @Test
    public void testSocialAggregateBusinessRules() {
        // 测试社交业务规则
        var userId = UserId.of(1L);
        var socialAggregate = SocialAggregate.create(userId);

        // 不能关注自己
        assertThrows(IllegalArgumentException.class, () -> {
            socialAggregate.followUser(userId);
        });

        // 不能重复关注
        var targetUserId = UserId.of(2L);
        socialAggregate.followUser(targetUserId);
        assertThrows(IllegalArgumentException.class, () -> {
            socialAggregate.followUser(targetUserId);
        });
    }

    @Test
    public void testFollowRelationCreation() {
        // 测试关注关系实体
        var followerId = UserId.of(1L);
        var followeeId = UserId.of(2L);

        var followRelation = FollowRelation.create(followerId, followeeId);

        assertNotNull(followRelation);
        assertEquals(followerId, followRelation.getFollowerId());
        assertEquals(followeeId, followRelation.getFolloweeId());
        assertTrue(followRelation.isActive());

        // 不能关注自己
        assertThrows(IllegalArgumentException.class, () -> {
            FollowRelation.create(followerId, followerId);
        });
    }

    @Test
    public void testCreateUserCommand() {
        // 测试创建用户命令
        var command = new CreateUserCommand(
                "13888888888",
                "testuser",
                "测试用户",
                "https://example.com/avatar.jpg",
                1,
                LocalDate.of(1990, 1, 1),
                "app"
        );

        assertNotNull(command);
        assertEquals("13888888888", command.mobile());
        assertEquals("testuser", command.username());
        assertEquals("测试用户", command.nickname());
    }

    @Test
    public void testFollowUserCommand() {
        // 测试关注用户命令
        var followerId = UserId.of(1L);
        var followeeId = UserId.of(2L);

        var command = new FollowUserCommand(followerId, followeeId);

        assertNotNull(command);
        assertEquals(followerId, command.followerId());
        assertEquals(followeeId, command.followeeId());

        // 不能关注自己
        assertThrows(IllegalArgumentException.class, () -> {
            new FollowUserCommand(followerId, followerId);
        });
    }

    @Test
    public void testInteractionAggregateCreation() {
        // 测试互动聚合根创建
        var targetId = TargetId.of("feed_123");
        var targetType = TargetType.FEED;
        var interactionAggregate = InteractionAggregate.create(targetId, targetType);

        assertNotNull(interactionAggregate);
        assertEquals(targetId, interactionAggregate.getTargetId());
        assertEquals(targetType, interactionAggregate.getTargetType());
        assertEquals(0, interactionAggregate.getLikeCount());
        assertEquals(0, interactionAggregate.getFavoriteCount());
        assertEquals(0, interactionAggregate.getCommentCount());
    }

    @Test
    public void testInteractionAggregateLike() {
        // 测试点赞功能
        var targetId = TargetId.of("feed_123");
        var targetType = TargetType.FEED;
        var userId = UserId.of(1L);
        var interactionAggregate = InteractionAggregate.create(targetId, targetType);

        var event = interactionAggregate.likeTarget(userId);

        assertNotNull(event);
        assertEquals("target.liked", event.eventType());
        assertTrue(interactionAggregate.isLikedBy(userId));
        assertEquals(1, interactionAggregate.getLikeCount());
        assertFalse(interactionAggregate.getDomainEvents().isEmpty());
    }

    @Test
    public void testInteractionAggregateFavorite() {
        // 测试收藏功能
        var targetId = TargetId.of("feed_123");
        var targetType = TargetType.FEED;
        var userId = UserId.of(1L);
        var interactionAggregate = InteractionAggregate.create(targetId, targetType);

        var event = interactionAggregate.favoriteTarget(userId);

        assertNotNull(event);
        assertEquals("target.favorited", event.eventType());
        assertTrue(interactionAggregate.isFavoritedBy(userId));
        assertEquals(1, interactionAggregate.getFavoriteCount());
        assertFalse(interactionAggregate.getDomainEvents().isEmpty());
    }

    @Test
    public void testInteractionAggregateComment() {
        // 测试评论功能
        var targetId = TargetId.of("feed_123");
        var targetType = TargetType.FEED;
        var userId = UserId.of(1L);
        var interactionAggregate = InteractionAggregate.create(targetId, targetType);

        var commentId = interactionAggregate.addComment(userId, "这是一条测试评论");

        assertNotNull(commentId);
        assertEquals(1, interactionAggregate.getCommentCount());
        assertFalse(interactionAggregate.getActiveComments().isEmpty());
    }

    @Test
    public void testInteractionAggregateBusinessRules() {
        // 测试互动业务规则
        var targetId = TargetId.of("feed_123");
        var targetType = TargetType.FEED;
        var userId = UserId.of(1L);
        var interactionAggregate = InteractionAggregate.create(targetId, targetType);

        // 点赞后不能重复点赞
        interactionAggregate.likeTarget(userId);
        assertThrows(IllegalArgumentException.class, () -> {
            interactionAggregate.likeTarget(userId);
        });

        // 收藏后不能重复收藏
        interactionAggregate.favoriteTarget(userId);
        assertThrows(IllegalArgumentException.class, () -> {
            interactionAggregate.favoriteTarget(userId);
        });

        // 评论内容不能为空
        assertThrows(IllegalArgumentException.class, () -> {
            interactionAggregate.addComment(userId, "");
        });

        // 评论内容不能超过500字符
        var longContent = "a".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> {
            interactionAggregate.addComment(userId, longContent);
        });
    }

    @Test
    public void testTargetTypeEnum() {
        // 测试目标类型枚举
        assertEquals("feed", TargetType.FEED.getCode());
        assertEquals("activity", TargetType.ACTIVITY.getCode());
        assertEquals("comment", TargetType.COMMENT.getCode());
        assertEquals("user", TargetType.USER.getCode());

        assertEquals(TargetType.FEED, TargetType.fromCode("feed"));
        assertEquals(TargetType.ACTIVITY, TargetType.fromCode("activity"));

        assertThrows(IllegalArgumentException.class, () -> {
            TargetType.fromCode("invalid_type");
        });
    }

    @Test
    public void testFeedContentCreation() {
        // 测试文本动态内容创建
        var textContent = FeedContent.createTextContent("这是一条测试动态");
        assertNotNull(textContent);
        assertEquals(FeedType.TEXT, textContent.getType());
        assertEquals("这是一条测试动态", textContent.getTextContent());

        // 测试图片动态内容创建
        var imageContent = FeedContent.createImageContent("图片动态", List.of("url1.jpg", "url2.jpg"));
        assertNotNull(imageContent);
        assertEquals(FeedType.IMAGE, imageContent.getType());
        assertEquals(2, imageContent.getMediaUrls().size());

        // 测试链接分享内容创建
        var linkContent = FeedContent.createLinkContent("分享链接", "https://example.com", "标题", "描述");
        assertNotNull(linkContent);
        assertEquals(FeedType.LINK, linkContent.getType());
        assertEquals("https://example.com", linkContent.getLinkUrl());
    }

    @Test
    public void testFeedAggregateCreation() {
        // 测试动态聚合根创建
        var authorId = UserId.of(1L);
        var content = FeedContent.createTextContent("测试动态内容");
        var settings = FeedSettings.defaultSettings();

        var feedAggregate = FeedAggregate.createDraft(authorId, content, settings);

        assertNotNull(feedAggregate);
        assertEquals(authorId, feedAggregate.getAuthorId());
        assertEquals(FeedStatus.DRAFT, feedAggregate.getStatus());
        assertEquals(0, feedAggregate.getViewCount());
        assertEquals(0, feedAggregate.getShareCount());
        assertNotNull(feedAggregate.getCreateTime());
    }

    @Test
    public void testFeedAggregatePublish() {
        // 测试动态发布
        var authorId = UserId.of(1L);
        var content = FeedContent.createTextContent("测试动态内容");
        var settings = FeedSettings.defaultSettings();

        var feedAggregate = FeedAggregate.createDraft(authorId, content, settings);
        var event = feedAggregate.publish();

        assertNotNull(event);
        assertEquals("feed.published", event.eventType());
        assertEquals(FeedStatus.PUBLISHED, feedAggregate.getStatus());
        assertNotNull(feedAggregate.getPublishTime());
        assertFalse(feedAggregate.getDomainEvents().isEmpty());
    }

    @Test
    public void testFeedAggregateBusinessRules() {
        // 测试动态业务规则
        var authorId = UserId.of(1L);
        var content = FeedContent.createTextContent("测试动态内容");
        var settings = FeedSettings.defaultSettings();

        var feedAggregate = FeedAggregate.createDraft(authorId, content, settings);

        // 发布后不能再发布
        feedAggregate.publish();
        assertThrows(IllegalStateException.class, () -> {
            feedAggregate.publish();
        });

        // 已发布的可以隐藏
        feedAggregate.hide();
        assertEquals(FeedStatus.HIDDEN, feedAggregate.getStatus());

        // 隐藏的可以显示
        feedAggregate.show();
        assertEquals(FeedStatus.PUBLISHED, feedAggregate.getStatus());
    }

    @Test
    public void testFeedSettings() {
        // 测试动态设置
        var defaultSettings = FeedSettings.defaultSettings();
        assertTrue(defaultSettings.isAllowComments());
        assertTrue(defaultSettings.isAllowLikes());
        assertTrue(defaultSettings.isAllowShares());
        assertTrue(defaultSettings.isPublic());

        var privateSettings = FeedSettings.privateSettings();
        assertFalse(privateSettings.isAllowComments());
        assertTrue(privateSettings.isPrivate());

        var customSettings = FeedSettings.customSettings(
                false, true, false,
                FeedSettings.VisibilityLevel.FOLLOWERS_ONLY, true
        );
        assertFalse(customSettings.isAllowComments());
        assertTrue(customSettings.isAllowLikes());
        assertFalse(customSettings.isAllowShares());
        assertEquals(FeedSettings.VisibilityLevel.FOLLOWERS_ONLY, customSettings.getVisibility());
    }

    @Test
    public void testFeedTypeEnum() {
        // 测试动态类型枚举
        assertEquals("text", FeedType.TEXT.getCode());
        assertEquals("image", FeedType.IMAGE.getCode());
        assertEquals("video", FeedType.VIDEO.getCode());
        assertEquals("link", FeedType.LINK.getCode());
        assertEquals("location", FeedType.LOCATION.getCode());

        assertTrue(FeedType.IMAGE.isMultimedia());
        assertTrue(FeedType.VIDEO.isMultimedia());
        assertFalse(FeedType.TEXT.isMultimedia());

        assertTrue(FeedType.ACTIVITY.requiresSpecialHandling());
        assertTrue(FeedType.TOPIC.requiresSpecialHandling());
        assertFalse(FeedType.TEXT.requiresSpecialHandling());

        assertEquals(FeedType.TEXT, FeedType.fromCode("text"));
        assertThrows(IllegalArgumentException.class, () -> {
            FeedType.fromCode("invalid_type");
        });
    }
}
