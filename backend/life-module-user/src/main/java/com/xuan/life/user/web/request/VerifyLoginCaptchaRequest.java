package com.xuan.life.user.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VerifyLoginCaptchaRequest(
    @NotBlank(message = "验证码标识不能为空")
    String captchaId,
    @NotEmpty(message = "请按提示完成点选验证码")
    @Size(min = 3, max = 3, message = "请按顺序点击 3 个字符")
    List<@Valid CaptchaClickPointRequest> captchaPoints
) {
}
