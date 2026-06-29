package com.xuan.life.user.web.response;

public record UserProfileResponse(
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
