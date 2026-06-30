package com.xuan.life.admin.web.response;

import java.util.List;

public record AdminOperatorResponse(
    Long userId,
    String username,
    String nickname,
    String avatarUrl,
    String platformRoleCode,
    List<String> adminRoleCodes,
    List<String> adminRoleNames
) {
}
