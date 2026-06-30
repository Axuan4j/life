package com.xuan.life.admin.web;

import com.xuan.life.admin.entity.AdminAccount;
import com.xuan.life.admin.model.AdminPermissionCodes;
import com.xuan.life.admin.service.AdminSecurityDetailsService;
import com.xuan.life.admin.web.request.AdminBroadcastMessageRequest;
import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.message.service.NotificationApplicationService;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/messages")
public class AdminMessageController {

    private final NotificationApplicationService notificationApplicationService;
    private final AdminSecurityDetailsService adminSecurityDetailsService;

    public AdminMessageController(
        NotificationApplicationService notificationApplicationService,
        AdminSecurityDetailsService adminSecurityDetailsService
    ) {
        this.notificationApplicationService = notificationApplicationService;
        this.adminSecurityDetailsService = adminSecurityDetailsService;
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.MESSAGE_BROADCAST + "')")
    public ApiResponse<Void> broadcast(
        @AuthenticationPrincipal LifeAuthenticatedUser currentUser,
        @Valid @RequestBody AdminBroadcastMessageRequest request
    ) {
        AdminAccount operator = adminSecurityDetailsService.getAccountOrThrow(currentUser.getUserId());
        notificationApplicationService.broadcastToUsers(resolveSenderName(operator), request.title(), request.contentText());
        return ApiResponse.success(null);
    }

    private String resolveSenderName(AdminAccount operator) {
        if (operator != null && StringUtils.hasText(operator.getDisplayName())) {
            return operator.getDisplayName().trim();
        }
        if (operator != null && StringUtils.hasText(operator.getUsername())) {
            return operator.getUsername().trim();
        }
        return "Life 管理后台";
    }
}
