package com.xuan.life.admin.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminBroadcastMessageRequest(
    @NotBlank(message = "广播标题不能为空")
    @Size(max = 64, message = "广播标题最多 64 个字符")
    String title,
    @NotBlank(message = "广播内容不能为空")
    @Size(max = 500, message = "广播内容最多 500 个字符")
    String contentText
) {
}
