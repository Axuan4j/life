package com.xuan.life.admin.web.response;

import java.util.List;

public record AdminMenuTreeItemResponse(
    Long id,
    Long parentId,
    String menuType,
    String menuName,
    String routeName,
    String routePath,
    String viewKey,
    String iconName,
    String permissionCode,
    Integer sortOrder,
    Integer visible,
    Integer status,
    Integer isSystem,
    List<AdminMenuTreeItemResponse> children
) {
}
