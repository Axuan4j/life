package com.xuan.life.user.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.user.service.UserProfileApplicationService;
import com.xuan.life.user.web.response.UserProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileApplicationService userProfileApplicationService;

    public UserController(UserProfileApplicationService userProfileApplicationService) {
        this.userProfileApplicationService = userProfileApplicationService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal LifeAuthenticatedUser currentUser) {
        return ApiResponse.success(userProfileApplicationService.getProfile(currentUser.getUserId()));
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
