package com.xuan.life.admin.web.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminAssignAdminUserRolesRequest(
    @NotNull(message = "角色列表不能为空")
    List<Long> roleIds
) {
}
