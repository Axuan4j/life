package com.xuan.life.social.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.social.service.FollowApplicationService;
import com.xuan.life.social.web.response.FollowStatusResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowApplicationService followApplicationService;

    public FollowController(FollowApplicationService followApplicationService) {
        this.followApplicationService = followApplicationService;
    }

    @PostMapping("/{targetUserId}")
    public ApiResponse<Void> follow(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("targetUserId") Long targetUserId
    ) {
        followApplicationService.follow(currentUser.getUserId(), targetUserId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{targetUserId}")
    public ApiResponse<Void> unfollow(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("targetUserId") Long targetUserId
    ) {
        followApplicationService.unfollow(currentUser.getUserId(), targetUserId);
        return ApiResponse.success(null);
    }

    @GetMapping("/me/following")
    public ApiResponse<List<Long>> myFollowing(@AuthenticationPrincipal LifeAuthenticatedUser currentUser) {
        return ApiResponse.success(followApplicationService.listFollowedUserIds(currentUser.getUserId()));
    }

    @GetMapping("/status/{targetUserId}")
    public ApiResponse<FollowStatusResponse> followStatus(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("targetUserId") Long targetUserId
    ) {
        return ApiResponse.success(new FollowStatusResponse(
            targetUserId,
            followApplicationService.isFollowing(currentUser.getUserId(), targetUserId)
        ));
    }
}
