package com.xuan.life.security.model;

public record TokenPair(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds
) {
}
