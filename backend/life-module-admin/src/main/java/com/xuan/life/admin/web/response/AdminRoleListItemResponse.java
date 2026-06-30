package com.xuan.life.admin.web.response;

public record AdminRoleListItemResponse(
    Long roleId,
    String roleCode,
    String roleName
) {
}
