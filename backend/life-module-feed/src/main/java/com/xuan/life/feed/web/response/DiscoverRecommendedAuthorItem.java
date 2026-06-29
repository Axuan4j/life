package com.xuan.life.feed.web.response;

public record DiscoverRecommendedAuthorItem(
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
