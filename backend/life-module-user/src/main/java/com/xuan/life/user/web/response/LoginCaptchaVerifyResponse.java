package com.xuan.life.user.web.response;

public record LoginCaptchaVerifyResponse(
    String tempKey,
    int expiresInSeconds
) {
}
