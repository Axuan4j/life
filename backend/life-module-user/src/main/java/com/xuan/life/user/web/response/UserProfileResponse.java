package com.xuan.life.user.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record UserProfileResponse(
    @JsonSerialize(using = ToStringSerializer.class)
    Long userId,
    String username,
    String nickname,
    String avatarUrl,
    String bio,
    String lastLoginRegion,
    long postCount,
    long followingCount,
    long followerCount,
    long likedCount
) {
}
