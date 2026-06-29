package com.xuan.life.content.web.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostCommentResponse(
    Long commentId,
    Long userId,
    String username,
    String nickname,
    String avatarUrl,
    String ipRegion,
    String contentText,
    LocalDateTime createdAt,
    List<PostCommentReplyResponse> replies
) {
}
