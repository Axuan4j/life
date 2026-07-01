package com.xuan.life.feed.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record DiscoverRecommendedAuthorItem(
    @JsonSerialize(using = ToStringSerializer.class)
    Long userId,
    String username,
    String nickname,
    String avatarUrl,
    String bio,
    long followerCount,
    boolean following,
    String reason
) {
}
