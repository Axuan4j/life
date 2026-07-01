package com.xuan.life.content.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record PostLikedUserResponse(
    @JsonSerialize(using = ToStringSerializer.class)
    Long userId,
    String username,
    String nickname,
    String avatarUrl
) {
}
