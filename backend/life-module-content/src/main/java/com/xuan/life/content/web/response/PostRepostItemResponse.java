package com.xuan.life.content.web.response;

import java.time.LocalDateTime;

public record PostRepostItemResponse(
    Long repostId,
    Long userId,
    String username,
    String nickname,
    String avatarUrl,
    String ipRegion,
    String contentText,
    LocalDateTime createdAt
) {
}
