package com.xuan.life.content.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.List;

public record PostCommentResponse(
    @JsonSerialize(using = ToStringSerializer.class)
    Long commentId,
    @JsonSerialize(using = ToStringSerializer.class)
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
