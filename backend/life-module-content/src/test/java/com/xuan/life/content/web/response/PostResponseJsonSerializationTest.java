package com.xuan.life.content.web.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostResponseJsonSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void shouldSerializePostIdsAsStringsButKeepCountersNumeric() throws Exception {
        PostCardResponse response = new PostCardResponse(
            9007199254740993L,
            9007199254740995L,
            "alice",
            "Alice",
            null,
            "上海",
            "PUBLIC",
            "hello",
            null,
            12L,
            3L,
            1L,
            List.of()
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"postId\":\"9007199254740993\"");
        assertThat(json).contains("\"authorId\":\"9007199254740995\"");
        assertThat(json).contains("\"likeCount\":12");
        assertThat(json).contains("\"commentCount\":3");
        assertThat(json).contains("\"repostCount\":1");
    }
}
