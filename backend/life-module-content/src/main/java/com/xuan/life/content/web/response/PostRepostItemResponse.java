package com.xuan.life.content.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public record PostRepostItemResponse(
    @JsonSerialize(using = ToStringSerializer.class)
    Long repostId,
    @JsonSerialize(using = ToStringSerializer.class)
    Long userId,
    String username,
    String nickname,
    String avatarUrl,
    String ipRegion,
    String contentText,
    LocalDateTime createdAt
) {
}
