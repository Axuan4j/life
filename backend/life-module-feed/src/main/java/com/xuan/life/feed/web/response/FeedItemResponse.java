package com.xuan.life.feed.web.response;

import com.xuan.life.content.web.response.PostCardResponse;

public record FeedItemResponse(
    String sourceType,
    PostCardResponse post
) {
}
