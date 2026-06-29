package com.xuan.life.feed.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.feed.service.DiscoverApplicationService;
import com.xuan.life.feed.web.response.DiscoverHomeResponse;
import com.xuan.life.feed.web.response.DiscoverResultPageResponse;
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
@RequestMapping("/api/discover")
public class DiscoverController {

    private final DiscoverApplicationService discoverApplicationService;

    public DiscoverController(DiscoverApplicationService discoverApplicationService) {
        this.discoverApplicationService = discoverApplicationService;
    }

    @GetMapping("/home")
    public ApiResponse<DiscoverHomeResponse> home(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser
    ) {
        return ApiResponse.success(discoverApplicationService.home(currentUser.getUserId()));
    }

    @GetMapping("/results")
    public ApiResponse<DiscoverResultPageResponse> results(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @RequestParam("type") String type,
        @RequestParam(name = "topicKey", required = false) String topicKey,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "sort", required = false, defaultValue = "COMPOSITE") String sort,
        @RequestParam(name = "cursor", required = false) String cursor,
        @RequestParam(name = "size", defaultValue = "10") @Min(value = 1, message = "size 必须大于 0") @Max(value = 30, message = "size 最大为 30") int size
    ) {
        return ApiResponse.success(discoverApplicationService.result(
            currentUser.getUserId(),
            type,
            topicKey,
            keyword,
            sort,
            cursor,
            size
        ));
    }
}
