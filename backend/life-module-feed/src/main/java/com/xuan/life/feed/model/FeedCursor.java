package com.xuan.life.feed.model;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

public record FeedCursor(
    LocalDateTime publishedAt,
    Long postId
) {
    public static FeedCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("_");
            long epochMilli = Long.parseLong(parts[0]);
            long postId = Long.parseLong(parts[1]);
            return new FeedCursor(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC), postId);
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的 feed cursor");
        }
    }

    public String encode() {
        String raw = publishedAt.toInstant(ZoneOffset.UTC).toEpochMilli() + "_" + postId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
