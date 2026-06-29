package com.xuan.life.content.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostCommentRequest(
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过 500 字")
    String contentText,
    Long parentCommentId,
    Long replyToUserId
) {
}
