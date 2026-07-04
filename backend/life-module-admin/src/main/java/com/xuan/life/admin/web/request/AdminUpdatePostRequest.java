package com.xuan.life.admin.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUpdatePostRequest(
    @NotNull(message = "帖子状态不能为空")
    @Min(value = 0, message = "帖子状态非法")
    @Max(value = 1, message = "帖子状态非法")
    Integer status,
    @NotNull(message = "审核状态不能为空")
    @Min(value = 0, message = "审核状态非法")
    @Max(value = 3, message = "审核状态非法")
    Integer reviewStatus,
    @Size(max = 255, message = "审核原因不能超过 255 个字符")
    String reviewReason,
    @NotBlank(message = "帖子可见性不能为空")
    String visibility
) {
}
