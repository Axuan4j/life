package com.xuan.life.content.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public record PostCommentReplyResponse(
    @JsonSerialize(using = ToStringSerializer.class)
    Long commentId,
    @JsonSerialize(using = ToStringSerializer.class)
    Long userId,
    String username,
    String nickname,
    String avatarUrl,
    String ipRegion,
    @JsonSerialize(using = ToStringSerializer.class)
    Long replyToUserId,
    String replyToUsername,
    String replyToNickname,
    String contentText,
    LocalDateTime createdAt
) {
}
