package com.xuan.life.content.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;

public record PostMediaRequest(
    @NotBlank(message = "媒体类型不能为空")
    String mediaType,
    @NotBlank(message = "媒体地址不能为空")
    String mediaUrl,
    @NotNull(message = "媒体顺序不能为空")
    @PositiveOrZero(message = "媒体顺序不能小于 0")
    Integer sortOrder
) {
}
