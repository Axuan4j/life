package com.xuan.life.feed.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.api.CursorPageResponse;
import com.xuan.life.feed.service.FeedApplicationService;
import com.xuan.life.feed.web.response.FeedItemResponse;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedApplicationService feedApplicationService;

    public FeedController(FeedApplicationService feedApplicationService) {
        this.feedApplicationService = feedApplicationService;
    }

    @GetMapping("/home")
    public ApiResponse<CursorPageResponse<FeedItemResponse>> homeFeed(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @RequestParam(name = "cursor", required = false) String cursor,
        @RequestParam(name = "size", defaultValue = "20") @Min(value = 1, message = "size 必须大于 0") @Max(value = 50, message = "size 最大为 50") int size
    ) {
        return ApiResponse.success(feedApplicationService.homeFeed(currentUser.getUserId(), cursor, size));
    }
}
