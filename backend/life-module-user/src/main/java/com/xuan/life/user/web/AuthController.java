package com.xuan.life.user.web;

import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.web.ClientIpResolver;
import com.xuan.life.security.model.TokenPair;
import com.xuan.life.user.service.AuthApplicationService;
import com.xuan.life.user.web.request.LoginRequest;
import com.xuan.life.user.web.request.RefreshTokenRequest;
import com.xuan.life.user.web.request.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final ClientIpResolver clientIpResolver;

    public AuthController(AuthApplicationService authApplicationService, ClientIpResolver clientIpResolver) {
        this.authApplicationService = authApplicationService;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/register")
    public ApiResponse<TokenPair> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.success(authApplicationService.register(request, clientIpResolver.resolve(httpServletRequest)));
    }

    @PostMapping("/login")
    public ApiResponse<TokenPair> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.success(authApplicationService.login(request, clientIpResolver.resolve(httpServletRequest)));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenPair> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authApplicationService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authApplicationService.logout();
        return ApiResponse.success(null);
    }
}
