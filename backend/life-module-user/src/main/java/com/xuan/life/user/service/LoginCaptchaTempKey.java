package com.xuan.life.user.service;

public record LoginCaptchaTempKey(
    String tempKey,
    String clientIp,
    long expiresAtEpochMilli
) {
}
