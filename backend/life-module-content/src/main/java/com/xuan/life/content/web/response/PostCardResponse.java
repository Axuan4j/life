package com.xuan.life.content.web.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostCardResponse(
    Long postId,
    Long authorId,
    String authorUsername,
    String authorNickname,
    String authorAvatarUrl,
    String ipRegion,
    String contentText,
    LocalDateTime publishedAt,
    Long likeCount,
    Long commentCount,
    Long repostCount,
    List<PostMediaResponse> medias
) {
}
