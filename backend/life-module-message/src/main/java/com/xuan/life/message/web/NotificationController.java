package com.xuan.life.message.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.api.PageResponse;
import com.xuan.life.message.service.NotificationApplicationService;
import com.xuan.life.message.web.response.UnreadCountResponse;
import com.xuan.life.message.web.response.UserNotificationResponse;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Validated
@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasRole('USER')")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    public NotificationController(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserNotificationResponse>> list(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @RequestParam(name = "pageNo", defaultValue = "1") @Min(1) int pageNo,
        @RequestParam(name = "pageSize", defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        return ApiResponse.success(notificationApplicationService.listNotifications(currentUser.getUserId(), pageNo, pageSize));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount(@AuthenticationPrincipal LifeAuthenticatedUser currentUser) {
        return ApiResponse.success(notificationApplicationService.unreadCount(currentUser.getUserId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markRead(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @PathVariable("notificationId") Long notificationId
    ) {
        notificationApplicationService.markRead(currentUser.getUserId(), notificationId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal LifeAuthenticatedUser currentUser) {
        notificationApplicationService.markAllRead(currentUser.getUserId());
        return ApiResponse.success(null);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal LifeAuthenticatedUser currentUser) {
        return notificationApplicationService.connect(currentUser.getUserId());
    }
}
