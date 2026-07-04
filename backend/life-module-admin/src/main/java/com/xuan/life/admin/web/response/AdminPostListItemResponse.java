package com.xuan.life.admin.web.response;

import java.time.LocalDateTime;

public record AdminPostListItemResponse(
    Long postId,
    Long authorId,
    String authorUsername,
    String authorNickname,
    String contentText,
    Integer status,
    Integer reviewStatus,
    String reviewReason,
    String visibility,
    LocalDateTime publishedAt,
    long likeCount,
    long commentCount,
    long repostCount
) {
}
