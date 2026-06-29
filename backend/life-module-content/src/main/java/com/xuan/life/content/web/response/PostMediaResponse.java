package com.xuan.life.content.web.response;

public record PostMediaResponse(
    String mediaType,
    String mediaUrl,
    Integer sortOrder
) {
}
