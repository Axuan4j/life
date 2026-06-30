package com.xuan.life.admin.web;

import com.xuan.life.admin.model.AdminPermissionCodes;
import com.xuan.life.admin.service.AdminRbacApplicationService;
import com.xuan.life.admin.web.request.AdminAssignAdminUserRolesRequest;
import com.xuan.life.admin.web.response.AdminAdminUserRoleItemResponse;
import com.xuan.life.common.api.ApiResponse;
import com.xuan.life.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/rbac/admin-users")
public class AdminUserRoleController {

    private final AdminRbacApplicationService adminRbacApplicationService;

    public AdminUserRoleController(AdminRbacApplicationService adminRbacApplicationService) {
        this.adminRbacApplicationService = adminRbacApplicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_ACCOUNT + "')")
    public ApiResponse<PageResponse<AdminAdminUserRoleItemResponse>> list(
        @RequestParam(name = "pageNo", defaultValue = "1") @Min(1) int pageNo,
        @RequestParam(name = "pageSize", defaultValue = "20") @Min(1) @Max(100) int pageSize,
        @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ApiResponse.success(adminRbacApplicationService.listAdminUsers(pageNo, pageSize, keyword));
    }

    @PatchMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('" + AdminPermissionCodes.RBAC_ACCOUNT + "')")
    public ApiResponse<Void> assignRoles(
        @PathVariable("userId") Long userId,
        @Valid @RequestBody AdminAssignAdminUserRolesRequest request
    ) {
        adminRbacApplicationService.assignAdminUserRoles(userId, request);
        return ApiResponse.success(null);
    }
}
