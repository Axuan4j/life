package com.xuan.life.admin.web.request;

import jakarta.validation.constraints.NotBlank;

public record AdminUpdatePostRequest(
    @NotBlank(message = "帖子状态不能为空")
    String status,
    @NotBlank(message = "帖子可见性不能为空")
    String visibility
) {
}
