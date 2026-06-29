package com.xuan.life.content.web.response;

public record PostDetailResponse(
    PostCardResponse post,
    PostInteractionResponse interaction
) {
}
