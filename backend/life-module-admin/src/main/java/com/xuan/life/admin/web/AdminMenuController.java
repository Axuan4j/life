package com.xuan.life.admin.web;

import com.xuan.life.admin.model.AdminPermissionCodes;
import com.xuan.life.admin.service.AdminRbacApplicationService;
import com.xuan.life.admin.web.request.AdminMenuSaveRequest;
import com.xuan.life.admin.web.response.AdminMenuTreeItemResponse;
import com.xuan.life.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rbac/menus")
public class AdminMenuController {

    private final AdminRbacApplicationService adminRbacApplicationService;

    public AdminMenuController(AdminRbacApplicationService adminRbacApplicationService) {
        this.adminRbacApplicationService = adminRbacApplicationService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_MENU + "')")
    public ApiResponse<List<AdminMenuTreeItemResponse>> tree() {
        return ApiResponse.success(adminRbacApplicationService.listMenuTree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_MENU + "')")
    public ApiResponse<Void> create(@Valid @RequestBody AdminMenuSaveRequest request) {
        adminRbacApplicationService.createMenu(request);
        return ApiResponse.success(null);
    }

    @PutMapping("/{menuId}")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_MENU + "')")
    public ApiResponse<Void> update(
        @PathVariable("menuId") Long menuId,
        @Valid @RequestBody AdminMenuSaveRequest request
    ) {
        adminRbacApplicationService.updateMenu(menuId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{menuId}")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_MENU + "')")
    public ApiResponse<Void> delete(@PathVariable("menuId") Long menuId) {
        adminRbacApplicationService.deleteMenu(menuId);
        return ApiResponse.success(null);
    }
}
