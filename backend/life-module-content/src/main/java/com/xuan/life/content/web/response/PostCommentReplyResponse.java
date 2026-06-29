package com.xuan.life.content.web.response;

import java.time.LocalDateTime;

public record PostCommentReplyResponse(
    Long commentId,
    Long userId,
    String username,
    String nickname,
    String avatarUrl,
    String ipRegion,
    Long replyToUserId,
    String replyToUsername,
    String replyToNickname,
    String contentText,
    LocalDateTime createdAt
) {
}
