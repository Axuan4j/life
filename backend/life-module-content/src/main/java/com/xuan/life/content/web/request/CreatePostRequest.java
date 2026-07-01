package com.xuan.life.content.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 2000, message = "帖子内容不能超过 2000 字")
    String contentText,
    String visibility,
    @Size(max = 9, message = "最多上传 9 个媒体")
    List<@Valid PostMediaRequest> medias
) {
}
