package com.xuan.life.admin.web;

import com.xuan.life.admin.service.AdminSessionApplicationService;
import com.xuan.life.admin.web.response.AdminSessionResponse;
import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/session")
public class AdminSessionController {

    private final AdminSessionApplicationService adminSessionApplicationService;

    public AdminSessionController(AdminSessionApplicationService adminSessionApplicationService) {
        this.adminSessionApplicationService = adminSessionApplicationService;
    }

    @GetMapping
    public ApiResponse<AdminSessionResponse> session(@AuthenticationPrincipal LifeAuthenticatedUser currentUser) {
        return ApiResponse.success(adminSessionApplicationService.session(currentUser.getUserId()));
    }
}
