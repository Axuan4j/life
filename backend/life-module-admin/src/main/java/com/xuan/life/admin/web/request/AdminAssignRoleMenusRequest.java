package com.xuan.life.admin.web.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminAssignRoleMenusRequest(
    @NotNull(message = "菜单列表不能为空")
    List<Long> menuIds
) {
}
