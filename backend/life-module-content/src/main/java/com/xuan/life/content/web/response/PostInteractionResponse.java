package com.xuan.life.content.web.response;

import java.util.List;

public record PostInteractionResponse(
    long likeCount,
    long commentCount,
    long repostCount,
    boolean likedByCurrentUser,
    boolean repostedByCurrentUser,
    List<PostLikedUserResponse> likedUsers
) {
}
