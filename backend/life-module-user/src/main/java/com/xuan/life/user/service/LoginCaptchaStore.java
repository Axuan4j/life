package com.xuan.life.user.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LoginCaptchaStore {

    private static final String CHALLENGE_KEY_PREFIX = "life:user:login-captcha:challenge:";
    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(2);
    private static final String TEMP_KEY_PREFIX = "life:user:login-captcha:temp-key:";
    private static final Duration TEMP_KEY_TTL = Duration.ofSeconds(90);

    private final RedisTemplate<String, Object> redisTemplate;

    public LoginCaptchaStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveChallenge(LoginCaptchaChallenge challenge) {
        redisTemplate.opsForValue().set(challengeKey(challenge.captchaId()), challenge, CHALLENGE_TTL);
    }

    public LoginCaptchaChallenge consumeChallenge(String captchaId) {
        Object remote = redisTemplate.opsForValue().getAndDelete(challengeKey(captchaId));
        if (remote instanceof LoginCaptchaChallenge challenge) {
            return challenge;
        }
        return null;
    }

    public void saveTempKey(LoginCaptchaTempKey tempKey) {
        redisTemplate.opsForValue().set(tempKeyKey(tempKey.tempKey()), tempKey, TEMP_KEY_TTL);
    }

    public LoginCaptchaTempKey consumeTempKey(String tempKey) {
        Object remote = redisTemplate.opsForValue().getAndDelete(tempKeyKey(tempKey));
        if (remote instanceof LoginCaptchaTempKey loginCaptchaTempKey) {
            return loginCaptchaTempKey;
        }
        return null;
    }

    private String challengeKey(String captchaId) {
        return CHALLENGE_KEY_PREFIX + captchaId;
    }

    private String tempKeyKey(String tempKey) {
        return TEMP_KEY_PREFIX + tempKey;
    }
}
