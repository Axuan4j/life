package com.xuan.life.social.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record FollowStatusResponse(
    @JsonSerialize(using = ToStringSerializer.class)
    Long targetUserId,
    boolean following
) {
}
