package com.xuan.life.security.service;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.security.config.JwtProperties;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.model.LifeRole;
import com.xuan.life.security.model.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public TokenPair issueTokenPair(LifeAuthenticatedUser user) {
        Instant now = Instant.now();
        Instant accessExpireAt = now.plus(jwtProperties.getAccessTokenExpireMinutes(), ChronoUnit.MINUTES);
        Instant refreshExpireAt = now.plus(jwtProperties.getRefreshTokenExpireDays(), ChronoUnit.DAYS);
        return new TokenPair(
            buildToken(user, "access", now, accessExpireAt),
            buildToken(user, "refresh", now, refreshExpireAt),
            "Bearer",
            jwtProperties.getAccessTokenExpireMinutes() * 60
        );
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parseToken(token);
        ensureTokenType(claims, "access");
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parseToken(token);
        ensureTokenType(claims, "refresh");
        return claims;
    }

    public LifeAuthenticatedUser toAuthenticatedUser(Claims claims) {
        Long userId = claims.get("uid", Long.class);
        String username = claims.getSubject();
        String roleCode = claims.get("role", String.class);
        return new LifeAuthenticatedUser(userId, username, "", LifeRole.fromCode(roleCode), true);
    }

    private String buildToken(LifeAuthenticatedUser user, String tokenType, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
            .subject(user.getUsername())
            .issuer(jwtProperties.getIssuer())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .claim("uid", user.getUserId())
            .claim("role", user.getRole().name())
            .claim("tokenType", tokenType)
            .signWith(signingKey())
            .compact();
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无效或已过期的登录凭证");
        }
    }

    private void ensureTokenType(Claims claims, String expectedType) {
        String tokenType = claims.get("tokenType", String.class);
        if (!expectedType.equals(tokenType)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录凭证类型不匹配");
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
