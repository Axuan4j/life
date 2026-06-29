package com.xuan.life.content.web.response;

public record PostLikedUserResponse(
    Long userId,
    String username,
    String nickname,
    String avatarUrl
) {
}
