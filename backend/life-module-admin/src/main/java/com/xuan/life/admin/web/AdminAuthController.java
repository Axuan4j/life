package com.xuan.life.admin.web;

import com.xuan.life.admin.service.AdminSessionApplicationService;
import com.xuan.life.admin.web.response.AdminLoginResponse;
import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.web.ClientIpResolver;
import com.xuan.life.security.model.TokenPair;
import com.xuan.life.user.web.request.LoginRequest;
import com.xuan.life.user.web.request.RefreshTokenRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminSessionApplicationService adminSessionApplicationService;
    private final ClientIpResolver clientIpResolver;

    public AdminAuthController(
        AdminSessionApplicationService adminSessionApplicationService,
        ClientIpResolver clientIpResolver
    ) {
        this.adminSessionApplicationService = adminSessionApplicationService;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(
            adminSessionApplicationService.login(request, clientIpResolver.resolve(httpServletRequest))
        );
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenPair> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(adminSessionApplicationService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        adminSessionApplicationService.logout();
        return ApiResponse.success(null);
    }
}
