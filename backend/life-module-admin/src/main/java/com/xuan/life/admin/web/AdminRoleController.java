package com.xuan.life.admin.web;

import com.xuan.life.admin.model.AdminPermissionCodes;
import com.xuan.life.admin.service.AdminRbacApplicationService;
import com.xuan.life.admin.web.request.AdminAssignRoleMenusRequest;
import com.xuan.life.admin.web.request.AdminRoleSaveRequest;
import com.xuan.life.admin.web.response.AdminRoleDetailResponse;
import com.xuan.life.admin.web.response.AdminRoleListItemResponse;
import com.xuan.life.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rbac/roles")
public class AdminRoleController {

    private final AdminRbacApplicationService adminRbacApplicationService;

    public AdminRoleController(AdminRbacApplicationService adminRbacApplicationService) {
        this.adminRbacApplicationService = adminRbacApplicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_ROLE + "')")
    public ApiResponse<List<AdminRoleDetailResponse>> list() {
        return ApiResponse.success(adminRbacApplicationService.listRoleDetails());
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_ROLE + "') or hasAuthority('" + AdminPermissionCodes.RBAC_ACCOUNT + "')")
    public ApiResponse<List<AdminRoleListItemResponse>> options() {
        return ApiResponse.success(adminRbacApplicationService.listRoleItems());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_ROLE + "')")
    public ApiResponse<Void> create(@Valid @RequestBody AdminRoleSaveRequest request) {
        adminRbacApplicationService.createRole(request);
        return ApiResponse.success(null);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_ROLE + "')")
    public ApiResponse<Void> update(
        @PathVariable("roleId") Long roleId,
        @Valid @RequestBody AdminRoleSaveRequest request
    ) {
        adminRbacApplicationService.updateRole(roleId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_ROLE + "')")
    public ApiResponse<Void> delete(@PathVariable("roleId") Long roleId) {
        adminRbacApplicationService.deleteRole(roleId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{roleId}/menus")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_ROLE + "')")
    public ApiResponse<Void> assignMenus(
        @PathVariable("roleId") Long roleId,
        @Valid @RequestBody AdminAssignRoleMenusRequest request
    ) {
        adminRbacApplicationService.assignRoleMenus(roleId, request);
        return ApiResponse.success(null);
    }
}
