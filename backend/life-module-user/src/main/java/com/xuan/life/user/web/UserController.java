package com.xuan.life.user.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.user.service.UserCheckInApplicationService;
import com.xuan.life.user.service.UserProfileApplicationService;
import com.xuan.life.user.web.response.UserCheckInStatusResponse;
import com.xuan.life.user.web.response.UserProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileApplicationService userProfileApplicationService;
    private final UserCheckInApplicationService userCheckInApplicationService;

    public UserController(
        UserProfileApplicationService userProfileApplicationService,
        UserCheckInApplicationService userCheckInApplicationService
    ) {
        this.userProfileApplicationService = userProfileApplicationService;
        this.userCheckInApplicationService = userCheckInApplicationService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal LifeAuthenticatedUser currentUser) {
        return ApiResponse.success(userProfileApplicationService.getOwnProfile(currentUser.getUserId()));
    }

    @GetMapping("/me/check-in")
    public ApiResponse<UserCheckInStatusResponse> checkInStatus(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser
    ) {
        return ApiResponse.success(userCheckInApplicationService.getStatus(currentUser.getUserId()));
    }

    @PostMapping("/me/check-in")
    public ApiResponse<UserCheckInStatusResponse> checkIn(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser
    ) {
        return ApiResponse.success(userCheckInApplicationService.checkIn(currentUser.getUserId()));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> profile(@PathVariable("userId") Long userId) {
        return ApiResponse.success(userProfileApplicationService.getProfile(userId));
    }

    @GetMapping("/{userId}/profile")
    public ApiResponse<UserProfileResponse> profileAlias(@PathVariable("userId") Long userId) {
        return ApiResponse.success(userProfileApplicationService.getProfile(userId));
    }
}
