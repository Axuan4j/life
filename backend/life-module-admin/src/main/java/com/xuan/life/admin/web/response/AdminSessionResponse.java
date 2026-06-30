package com.xuan.life.admin.web.response;

import java.util.List;

public record AdminSessionResponse(
    AdminOperatorResponse operator,
    List<AdminMenuTreeItemResponse> menus,
    List<String> permissions,
    String homePath
) {
}
