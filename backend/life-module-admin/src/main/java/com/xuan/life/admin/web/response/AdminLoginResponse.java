package com.xuan.life.admin.web.response;

import java.util.List;

public record AdminLoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    AdminOperatorResponse operator,
    List<AdminMenuTreeItemResponse> menus,
    List<String> permissions,
    String homePath
) {
}
