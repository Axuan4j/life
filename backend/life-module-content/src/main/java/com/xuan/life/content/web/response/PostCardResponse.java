package com.xuan.life.content.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.List;

public record PostCardResponse(
    @JsonSerialize(using = ToStringSerializer.class)
    Long postId,
    @JsonSerialize(using = ToStringSerializer.class)
    Long authorId,
    String authorUsername,
    String authorNickname,
    String authorAvatarUrl,
    String ipRegion,
    String visibility,
    String contentText,
    LocalDateTime publishedAt,
    Long likeCount,
    Long commentCount,
    Long repostCount,
    List<PostMediaResponse> medias
) {
}
