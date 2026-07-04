package com.xuan.life.content.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.web.ClientIpResolver;
import com.xuan.life.content.service.PostApplicationService;
import com.xuan.life.content.web.request.CreatePostCommentRequest;
import com.xuan.life.content.web.request.CreatePostRequest;
import com.xuan.life.content.web.request.VotePostPollRequest;
import com.xuan.life.content.web.response.CreatePostResponse;
import com.xuan.life.content.web.response.PostCardResponse;
import com.xuan.life.content.web.response.PostCommentResponse;
import com.xuan.life.content.web.response.PostDetailResponse;
import com.xuan.life.content.web.response.PostInteractionResponse;
import com.xuan.life.content.web.response.PostPollStateResponse;
import com.xuan.life.content.web.response.PostRepostItemResponse;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostApplicationService postApplicationService;
    private final ClientIpResolver clientIpResolver;

    public PostController(PostApplicationService postApplicationService, ClientIpResolver clientIpResolver) {
        this.postApplicationService = postApplicationService;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping
    public ApiResponse<CreatePostResponse> create(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        HttpServletRequest httpServletRequest,
        @Valid @RequestBody CreatePostRequest request
    ) {
        return ApiResponse.success(postApplicationService.createPost(
            currentUser.getUserId(),
            request,
            clientIpResolver.resolve(httpServletRequest)
        ));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<List<PostCardResponse>> listUserPosts(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("userId") Long userId,
        @RequestParam(name = "pageNo", defaultValue = "1") @Min(value = 1, message = "页码必须大于 0") int pageNo,
        @RequestParam(name = "pageSize", defaultValue = "20") @Min(value = 1, message = "每页条数必须大于 0") @Max(value = 50, message = "每页最多 50 条") int pageSize
    ) {
        return ApiResponse.success(postApplicationService.listUserPosts(
            currentUser != null ? currentUser.getUserId() : null,
            userId,
            pageNo,
            pageSize
        ));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> detail(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postApplicationService.getPostDetail(currentUser.getUserId(), postId));
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<PostCommentResponse>> comments(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postApplicationService.listPostComments(
            currentUser != null ? currentUser.getUserId() : null,
            postId
        ));
    }

    @GetMapping("/{postId}/reposts")
    public ApiResponse<List<PostRepostItemResponse>> reposts(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postApplicationService.listPostReposts(
            currentUser != null ? currentUser.getUserId() : null,
            postId
        ));
    }

    @GetMapping("/{postId}/poll")
    public ApiResponse<PostPollStateResponse> poll(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postApplicationService.getPostPollState(
            currentUser != null ? currentUser.getUserId() : null,
            postId
        ));
    }

    @PostMapping("/{postId}/poll/vote")
    public ApiResponse<PostPollStateResponse> votePoll(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId,
        @Valid @RequestBody VotePostPollRequest request
    ) {
        return ApiResponse.success(postApplicationService.votePoll(currentUser.getUserId(), postId, request));
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<Void> createComment(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId,
        HttpServletRequest httpServletRequest,
        @Valid @RequestBody CreatePostCommentRequest request
    ) {
        postApplicationService.createComment(
            currentUser.getUserId(),
            postId,
            request,
            clientIpResolver.resolve(httpServletRequest)
        );
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId,
        @PathVariable("commentId") Long commentId
    ) {
        postApplicationService.deleteComment(currentUser.getUserId(), postId, commentId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{postId}/likes/toggle")
    public ApiResponse<PostInteractionResponse> toggleLike(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postApplicationService.toggleLike(currentUser.getUserId(), postId));
    }

    @PostMapping("/{postId}/reposts")
    public ApiResponse<PostInteractionResponse> repost(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postApplicationService.repost(currentUser.getUserId(), postId));
    }
}
