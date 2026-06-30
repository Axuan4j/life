package com.xuan.life.admin.web.response;

import java.time.LocalDateTime;

public record AdminUserListItemResponse(
    Long userId,
    String username,
    String nickname,
    String roleCode,
    Integer status,
    LocalDateTime createdAt,
    long postCount,
    long followingCount,
    long followerCount
) {
}
