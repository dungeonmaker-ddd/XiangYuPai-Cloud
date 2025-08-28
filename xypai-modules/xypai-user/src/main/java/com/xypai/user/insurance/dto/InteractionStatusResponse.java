package com.xypai.user.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 互动状态响应DTO
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Schema(description = "互动状态信息")
public record InteractionStatusResponse(
        @Schema(description = "是否已点赞", example = "true")
        boolean isLiked,

        @Schema(description = "是否已收藏", example = "false")
        boolean isFavorited,

        @Schema(description = "点赞数", example = "128")
        int likeCount,

        @Schema(description = "收藏数", example = "56")
        int favoriteCount,

        @Schema(description = "评论数", example = "23")
        int commentCount
) {
}
