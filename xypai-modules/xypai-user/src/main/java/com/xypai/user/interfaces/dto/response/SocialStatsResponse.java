package com.xypai.user.interfaces.dto.response;

import com.xypai.user.domain.social.SocialAggregate;
import com.xypai.user.domain.user.valueobject.UserId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * 社交统计响应DTO
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Schema(description = "社交统计信息")
public record SocialStatsResponse(
        @Schema(description = "用户ID")
        UserId userId,

        @Schema(description = "关注数", example = "128")
        int followingCount,

        @Schema(description = "粉丝数", example = "256")
        int followerCount,

        @Schema(description = "好友数", example = "64")
        int friendCount
) {

    public SocialStatsResponse {
        Objects.requireNonNull(userId, "用户ID不能为空");
        if (followingCount < 0 || followerCount < 0 || friendCount < 0) {
            throw new IllegalArgumentException("统计数量不能为负数");
        }
    }

    /**
     * 静态工厂方法：从SocialAggregate创建SocialStatsResponse
     */
    public static SocialStatsResponse fromAggregate(SocialAggregate aggregate) {
        Objects.requireNonNull(aggregate, "SocialAggregate不能为空");
        return new SocialStatsResponse(
                aggregate.getUserId(),
                aggregate.getFollowingCount(),
                aggregate.getFollowerCount(),
                0 // 好友数暂未实现
        );
    }
}
