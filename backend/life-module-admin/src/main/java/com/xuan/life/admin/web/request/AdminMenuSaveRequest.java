package com.xuan.life.admin.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminMenuSaveRequest(
    Long parentId,
    @NotBlank(message = "菜单类型不能为空")
    String menuType,
    @NotBlank(message = "菜单名称不能为空")
    String menuName,
    @NotBlank(message = "路由名称不能为空")
    String routeName,
    @NotBlank(message = "路由路径不能为空")
    String routePath,
    String viewKey,
    String iconName,
    String permissionCode,
    @NotNull(message = "排序不能为空")
    Integer sortOrder,
    @NotNull(message = "显示状态不能为空")
    @Min(value = 0, message = "显示状态非法")
    @Max(value = 1, message = "显示状态非法")
    Integer visible,
    @NotNull(message = "启用状态不能为空")
    @Min(value = 0, message = "启用状态非法")
    @Max(value = 1, message = "启用状态非法")
    Integer status
) {
}
