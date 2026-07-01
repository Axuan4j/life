package com.xuan.life.user.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CaptchaClickPointRequest(
    @NotNull(message = "验证码坐标不能为空")
    @Min(value = 0, message = "验证码坐标无效")
    Integer x,
    @NotNull(message = "验证码坐标不能为空")
    @Min(value = 0, message = "验证码坐标无效")
    Integer y
) {
}
