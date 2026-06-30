package com.xuan.life.admin.web.response;

import java.time.LocalDateTime;
import java.util.List;

public record AdminAdminUserRoleItemResponse(
    Long userId,
    String username,
    String nickname,
    Integer status,
    String platformRoleCode,
    LocalDateTime createdAt,
    List<Long> roleIds,
    List<AdminRoleListItemResponse> roles
) {
}
