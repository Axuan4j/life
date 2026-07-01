package com.xuan.life.user.web.response;

import java.util.List;

public record LoginCaptchaResponse(
    String captchaId,
    String imageData,
    List<String> targets,
    int width,
    int height,
    int expiresInSeconds
) {
}
