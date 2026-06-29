package com.xuan.life.feed.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoverCursorTest {

    @Test
    void shouldEncodeAndDecodeCursor() {
        DiscoverCursor cursor = new DiscoverCursor(
            3_240L,
            LocalDateTime.of(2026, 6, 29, 12, 30, 0),
            30001L
        );

        String encoded = cursor.encode();

        assertThat(DiscoverCursor.decode(encoded)).isEqualTo(cursor);
    }
}
