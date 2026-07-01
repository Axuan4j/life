package com.xuan.life.user.service;

import java.util.List;

public record LoginCaptchaChallenge(
    String captchaId,
    List<LoginCaptchaTarget> targets,
    long expiresAtEpochMilli
) {
}
