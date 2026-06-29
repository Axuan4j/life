package com.xuan.life.social.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.social.service.FollowApplicationService;
import com.xuan.life.user.service.UserProfileApplicationService;
import com.xuan.life.user.web.response.UserProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowQueryController {

    private final FollowApplicationService followApplicationService;
    private final UserProfileApplicationService userProfileApplicationService;

    public FollowQueryController(
        FollowApplicationService followApplicationService,
        UserProfileApplicationService userProfileApplicationService
    ) {
        this.followApplicationService = followApplicationService;
        this.userProfileApplicationService = userProfileApplicationService;
    }

    @GetMapping("/me/following/profiles")
    public ApiResponse<List<UserProfileResponse>> myFollowingProfiles(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser
    ) {
        List<Long> followedUserIds = followApplicationService.listFollowedUserIds(currentUser.getUserId());
        Map<Long, UserProfileResponse> profileMap = userProfileApplicationService.getProfiles(followedUserIds);
        List<UserProfileResponse> profiles = followedUserIds.stream()
            .map(profileMap::get)
            .filter(java.util.Objects::nonNull)
            .toList();
        return ApiResponse.success(profiles);
    }
}
