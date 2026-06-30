package com.xuan.life.admin.service;

import com.xuan.life.admin.entity.AdminAccount;
import com.xuan.life.admin.entity.AdminMenu;
import com.xuan.life.admin.entity.AdminRole;
import com.xuan.life.admin.model.AdminMenuType;
import com.xuan.life.admin.web.response.AdminLoginResponse;
import com.xuan.life.admin.web.response.AdminOperatorResponse;
import com.xuan.life.admin.web.response.AdminSessionResponse;
import com.xuan.life.security.model.LifeRole;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.model.TokenPair;
import com.xuan.life.security.service.JwtTokenService;
import com.xuan.life.user.web.request.LoginRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminSessionApplicationService {

    private final AdminSecurityDetailsService adminSecurityDetailsService;
    private final JwtTokenService jwtTokenService;
    private final AdminPermissionService adminPermissionService;
    private final AdminMenuTreeBuilder adminMenuTreeBuilder;

    public AdminSessionApplicationService(
        AdminSecurityDetailsService adminSecurityDetailsService,
        JwtTokenService jwtTokenService,
        AdminPermissionService adminPermissionService,
        AdminMenuTreeBuilder adminMenuTreeBuilder
    ) {
        this.adminSecurityDetailsService = adminSecurityDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.adminPermissionService = adminPermissionService;
        this.adminMenuTreeBuilder = adminMenuTreeBuilder;
    }

    public AdminLoginResponse login(LoginRequest request, String clientIp) {
        LifeAuthenticatedUser authenticatedUser = adminSecurityDetailsService.authenticate(
            request.username(),
            request.password(),
            clientIp
        );
        TokenPair tokenPair = jwtTokenService.issueTokenPair(authenticatedUser);
        AdminSessionResponse session = session(authenticatedUser.getUserId());
        return new AdminLoginResponse(
            tokenPair.accessToken(),
            tokenPair.refreshToken(),
            tokenPair.tokenType(),
            tokenPair.expiresInSeconds(),
            session.operator(),
            session.menus(),
            session.permissions(),
            session.homePath()
        );
    }

    public AdminSessionResponse session(Long userId) {
        AdminAccount account = adminSecurityDetailsService.getAccountOrThrow(userId);

        List<AdminRole> adminRoles = adminPermissionService.listRolesByUserId(userId, false);
        List<AdminMenu> sessionMenus = adminPermissionService.listSessionMenus(userId);
        List<String> permissions = adminPermissionService.listPermissionCodes(userId);

        return new AdminSessionResponse(
            new AdminOperatorResponse(
                account.getId(),
                account.getUsername(),
                account.getDisplayName() != null && !account.getDisplayName().isBlank() ? account.getDisplayName() : account.getUsername(),
                "",
                LifeRole.ADMIN.name(),
                adminRoles.stream().map(AdminRole::getRoleCode).toList(),
                adminRoles.stream().map(AdminRole::getRoleName).toList()
            ),
            adminMenuTreeBuilder.buildTree(sessionMenus),
            permissions,
            resolveHomePath(sessionMenus)
        );
    }

    private String resolveHomePath(List<AdminMenu> menus) {
        return menus.stream()
            .filter(menu -> menu.getVisible() != null && menu.getVisible() == 1)
            .filter(menu -> menu.getStatus() != null && menu.getStatus() == 1)
            .filter(menu -> AdminMenuType.PAGE.name().equalsIgnoreCase(menu.getMenuType()))
            .map(AdminMenu::getRoutePath)
            .filter(path -> path != null && !path.isBlank())
            .findFirst()
            .orElse("/403");
    }
}
