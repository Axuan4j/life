package com.xuan.life.feed.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class FeedCursorTest {

    @Test
    void shouldEncodeAndDecodeCursor() {
        FeedCursor cursor = new FeedCursor(LocalDateTime.of(2026, 6, 28, 12, 0), 1001L);
        String encoded = cursor.encode();
        FeedCursor decoded = FeedCursor.decode(encoded);
        Assertions.assertEquals(cursor.publishedAt(), decoded.publishedAt());
        Assertions.assertEquals(cursor.postId(), decoded.postId());
    }
}
