package com.xuan.life.social.web.response;

public record FollowStatusResponse(
    Long targetUserId,
    boolean following
) {
}
