package com.xuan.life.user.service;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.user.web.request.CaptchaClickPointRequest;
import com.xuan.life.user.web.request.VerifyLoginCaptchaRequest;
import com.xuan.life.user.web.response.LoginCaptchaResponse;
import com.xuan.life.user.web.response.LoginCaptchaVerifyResponse;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class LoginCaptchaService {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 180;
    private static final int COLUMN_COUNT = 3;
    private static final int ROW_COUNT = 3;
    private static final int CELL_COUNT = COLUMN_COUNT * ROW_COUNT;
    private static final int TARGET_COUNT = 3;
    private static final int VERIFY_RADIUS = 26;
    private static final int CHALLENGE_EXPIRE_SECONDS = (int) Duration.ofMinutes(2).toSeconds();
    private static final int TEMP_KEY_EXPIRE_SECONDS = (int) Duration.ofSeconds(90).toSeconds();
    private static final String CHAR_POOL = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String[] TEXT_COLORS = {"#243b53", "#1f5f8b", "#7c3aed", "#ef6c57", "#0f766e", "#9a3412"};
    private static final String[] SHAPE_COLORS = {"#ffe7dc", "#e7f3ff", "#fff3c4", "#f4e8ff", "#dff7f0"};

    private final SecureRandom secureRandom = new SecureRandom();
    private final LoginCaptchaStore loginCaptchaStore;

    public LoginCaptchaService(LoginCaptchaStore loginCaptchaStore) {
        this.loginCaptchaStore = loginCaptchaStore;
    }

    public LoginCaptchaResponse createCaptcha() {
        List<String> characters = randomCharacters(CELL_COUNT);
        List<CaptchaGlyph> glyphs = buildGlyphs(characters);

        List<CaptchaGlyph> shuffledGlyphs = new ArrayList<>(glyphs);
        Collections.shuffle(shuffledGlyphs, secureRandom);
        List<CaptchaGlyph> targetGlyphs = new ArrayList<>(shuffledGlyphs.subList(0, TARGET_COUNT));
        List<String> targets = targetGlyphs.stream().map(CaptchaGlyph::text).toList();

        String captchaId = UUID.randomUUID().toString().replace("-", "");
        long expiresAtEpochMilli = Instant.now().plusSeconds(CHALLENGE_EXPIRE_SECONDS).toEpochMilli();
        LoginCaptchaChallenge challenge = new LoginCaptchaChallenge(
            captchaId,
            targetGlyphs.stream().map(glyph -> new LoginCaptchaTarget(glyph.text(), glyph.centerX(), glyph.centerY())).toList(),
            expiresAtEpochMilli
        );
        loginCaptchaStore.saveChallenge(challenge);

        String svg = renderSvg(glyphs);
        return new LoginCaptchaResponse(
            captchaId,
            encodeSvg(svg),
            targets,
            WIDTH,
            HEIGHT,
            CHALLENGE_EXPIRE_SECONDS
        );
    }

    public LoginCaptchaVerifyResponse verifyAndIssueTempKey(VerifyLoginCaptchaRequest request, String clientIp) {
        List<CaptchaClickPointRequest> clickPoints = request.captchaPoints();
        if (clickPoints == null || clickPoints.size() != TARGET_COUNT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请按提示完成点选验证码");
        }

        LoginCaptchaChallenge challenge = loginCaptchaStore.consumeChallenge(request.captchaId());
        if (challenge == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已失效，请刷新后重试");
        }
        if (challenge.expiresAtEpochMilli() <= System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已过期，请刷新后重试");
        }

        for (int i = 0; i < challenge.targets().size(); i++) {
            LoginCaptchaTarget target = challenge.targets().get(i);
            CaptchaClickPointRequest point = clickPoints.get(i);
            if (!withinBounds(point.x(), point.y())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码校验失败，请重新点选");
            }
            if (!isWithinTarget(point.x(), point.y(), target.x(), target.y())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码校验失败，请重新点选");
            }
        }

        String tempKey = UUID.randomUUID().toString().replace("-", "");
        loginCaptchaStore.saveTempKey(new LoginCaptchaTempKey(
            tempKey,
            clientIp,
            Instant.now().plusSeconds(TEMP_KEY_EXPIRE_SECONDS).toEpochMilli()
        ));
        return new LoginCaptchaVerifyResponse(tempKey, TEMP_KEY_EXPIRE_SECONDS);
    }

    public void consumeAndValidateTempKey(String tempKey, String clientIp) {
        if (!StringUtils.hasText(tempKey)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先完成安全验证");
        }
        LoginCaptchaTempKey loginCaptchaTempKey = loginCaptchaStore.consumeTempKey(tempKey);
        if (loginCaptchaTempKey == null || loginCaptchaTempKey.expiresAtEpochMilli() <= System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录验证已失效，请重新验证");
        }
        if (!sameClient(loginCaptchaTempKey.clientIp(), clientIp)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录验证环境已变更，请重新验证");
        }
    }

    private List<String> randomCharacters(int count) {
        List<String> pool = new ArrayList<>();
        for (int i = 0; i < CHAR_POOL.length(); i++) {
            pool.add(String.valueOf(CHAR_POOL.charAt(i)));
        }
        Collections.shuffle(pool, secureRandom);
        return new ArrayList<>(pool.subList(0, count));
    }

    private List<CaptchaGlyph> buildGlyphs(List<String> characters) {
        List<CaptchaGlyph> glyphs = new ArrayList<>();
        double cellWidth = (double) WIDTH / COLUMN_COUNT;
        double cellHeight = (double) HEIGHT / ROW_COUNT;
        for (int index = 0; index < characters.size(); index++) {
            int row = index / COLUMN_COUNT;
            int column = index % COLUMN_COUNT;
            int centerX = (int) Math.round(column * cellWidth + cellWidth / 2 + randomOffset(16));
            int centerY = (int) Math.round(row * cellHeight + cellHeight / 2 + randomOffset(12));
            int fontSize = randomBetween(30, 42);
            int rotation = randomBetween(-20, 20);
            String color = TEXT_COLORS[randomBetween(0, TEXT_COLORS.length - 1)];
            glyphs.add(new CaptchaGlyph(characters.get(index), centerX, centerY, fontSize, rotation, color));
        }
        return glyphs;
    }

    private String renderSvg(List<CaptchaGlyph> glyphs) {
        StringBuilder svg = new StringBuilder(2048);
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(WIDTH)
            .append("\" height=\"").append(HEIGHT)
            .append("\" viewBox=\"0 0 ").append(WIDTH).append(' ').append(HEIGHT).append("\">")
            .append("<defs>")
            .append("<linearGradient id=\"bg\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">")
            .append("<stop offset=\"0%\" stop-color=\"#fffaf5\"/>")
            .append("<stop offset=\"100%\" stop-color=\"#fff2e8\"/>")
            .append("</linearGradient>")
            .append("</defs>")
            .append("<rect width=\"100%\" height=\"100%\" rx=\"24\" fill=\"url(#bg)\"/>");

        for (int i = 0; i < 8; i++) {
            int radius = randomBetween(10, 28);
            int cx = randomBetween(radius, WIDTH - radius);
            int cy = randomBetween(radius, HEIGHT - radius);
            String fill = SHAPE_COLORS[randomBetween(0, SHAPE_COLORS.length - 1)];
            svg.append("<circle cx=\"").append(cx).append("\" cy=\"").append(cy)
                .append("\" r=\"").append(radius)
                .append("\" fill=\"").append(fill)
                .append("\" fill-opacity=\"0.75\"/>");
        }

        for (int i = 0; i < 7; i++) {
            svg.append("<path d=\"M")
                .append(randomBetween(0, WIDTH / 3)).append(' ')
                .append(randomBetween(0, HEIGHT))
                .append(" C ")
                .append(randomBetween(WIDTH / 4, WIDTH / 2)).append(' ')
                .append(randomBetween(0, HEIGHT)).append(", ")
                .append(randomBetween(WIDTH / 2, WIDTH)).append(' ')
                .append(randomBetween(0, HEIGHT)).append(", ")
                .append(randomBetween(WIDTH / 2, WIDTH)).append(' ')
                .append(randomBetween(0, HEIGHT))
                .append("\" fill=\"none\" stroke=\"#f0cbb5\" stroke-width=\"")
                .append(randomBetween(1, 2))
                .append("\" stroke-opacity=\"0.55\"/>");
        }

        for (CaptchaGlyph glyph : glyphs) {
            svg.append("<text x=\"").append(glyph.centerX())
                .append("\" y=\"").append(glyph.centerY())
                .append("\" text-anchor=\"middle\" dominant-baseline=\"middle\" font-size=\"")
                .append(glyph.fontSize())
                .append("\" font-family=\"Arial, PingFang SC, sans-serif\" font-weight=\"700\" fill=\"")
                .append(glyph.color())
                .append("\" transform=\"rotate(")
                .append(glyph.rotation()).append(' ')
                .append(glyph.centerX()).append(' ')
                .append(glyph.centerY()).append(")\">")
                .append(escapeXml(glyph.text()))
                .append("</text>");
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private String encodeSvg(String svg) {
        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }

    private boolean isWithinTarget(int x, int y, int targetX, int targetY) {
        int deltaX = x - targetX;
        int deltaY = y - targetY;
        return deltaX * deltaX + deltaY * deltaY <= VERIFY_RADIUS * VERIFY_RADIUS;
    }

    private boolean withinBounds(int x, int y) {
        return x >= 0 && x <= WIDTH && y >= 0 && y <= HEIGHT;
    }

    private boolean sameClient(String issuedClientIp, String currentClientIp) {
        if (!StringUtils.hasText(issuedClientIp) || !StringUtils.hasText(currentClientIp)) {
            return false;
        }
        return issuedClientIp.equals(currentClientIp);
    }

    private int randomBetween(int min, int max) {
        return secureRandom.nextInt(max - min + 1) + min;
    }

    private int randomOffset(int limit) {
        return secureRandom.nextInt(limit * 2 + 1) - limit;
    }

    private String escapeXml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private record CaptchaGlyph(
        String text,
        int centerX,
        int centerY,
        int fontSize,
        int rotation,
        String color
    ) {
    }
}
