package com.xuan.life.admin.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminRoleSaveRequest(
    @NotBlank(message = "角色编码不能为空")
    String roleCode,
    @NotBlank(message = "角色名称不能为空")
    String roleName,
    String remark,
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态非法")
    @Max(value = 1, message = "状态非法")
    Integer status
) {
}
