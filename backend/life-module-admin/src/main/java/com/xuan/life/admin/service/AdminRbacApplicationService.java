package com.xuan.life.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.life.admin.entity.AdminAccount;
import com.xuan.life.admin.entity.AdminMenu;
import com.xuan.life.admin.entity.AdminRole;
import com.xuan.life.admin.entity.AdminRoleMenu;
import com.xuan.life.admin.entity.AdminUserRole;
import com.xuan.life.admin.mapper.AdminAccountMapper;
import com.xuan.life.admin.mapper.AdminMenuMapper;
import com.xuan.life.admin.mapper.AdminRoleMapper;
import com.xuan.life.admin.mapper.AdminRoleMenuMapper;
import com.xuan.life.admin.mapper.AdminUserRoleMapper;
import com.xuan.life.admin.model.AdminMenuType;
import com.xuan.life.admin.model.AdminRoleCodes;
import com.xuan.life.admin.web.request.AdminAssignAdminUserRolesRequest;
import com.xuan.life.admin.web.request.AdminAssignRoleMenusRequest;
import com.xuan.life.admin.web.request.AdminMenuSaveRequest;
import com.xuan.life.admin.web.request.AdminRoleSaveRequest;
import com.xuan.life.admin.web.response.AdminAdminUserRoleItemResponse;
import com.xuan.life.admin.web.response.AdminMenuTreeItemResponse;
import com.xuan.life.admin.web.response.AdminRoleDetailResponse;
import com.xuan.life.admin.web.response.AdminRoleListItemResponse;
import com.xuan.life.common.api.PageResponse;
import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.security.model.LifeRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminRbacApplicationService {

    private final AdminMenuMapper adminMenuMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRoleMenuMapper adminRoleMenuMapper;
    private final AdminAccountMapper adminAccountMapper;
    private final AdminMenuTreeBuilder adminMenuTreeBuilder;
    private final AdminPermissionService adminPermissionService;

    public AdminRbacApplicationService(
        AdminMenuMapper adminMenuMapper,
        AdminRoleMapper adminRoleMapper,
        AdminUserRoleMapper adminUserRoleMapper,
        AdminRoleMenuMapper adminRoleMenuMapper,
        AdminAccountMapper adminAccountMapper,
        AdminMenuTreeBuilder adminMenuTreeBuilder,
        AdminPermissionService adminPermissionService
    ) {
        this.adminMenuMapper = adminMenuMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminRoleMenuMapper = adminRoleMenuMapper;
        this.adminAccountMapper = adminAccountMapper;
        this.adminMenuTreeBuilder = adminMenuTreeBuilder;
        this.adminPermissionService = adminPermissionService;
    }

    public List<AdminMenuTreeItemResponse> listMenuTree() {
        return adminMenuTreeBuilder.buildTree(adminPermissionService.listAllMenus());
    }

    @Transactional
    public void createMenu(AdminMenuSaveRequest request) {
        AdminMenu menu = new AdminMenu();
        applyMenuRequest(menu, request, null);
        menu.setIsSystem(0);
        adminMenuMapper.insert(menu);
    }

    @Transactional
    public void updateMenu(Long menuId, AdminMenuSaveRequest request) {
        AdminMenu menu = getMenuOrThrow(menuId);
        if (menu.getIsSystem() != null && menu.getIsSystem() == 1 && request.status() != null && request.status() == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "系统菜单不能禁用");
        }
        applyMenuRequest(menu, request, menuId);
        adminMenuMapper.updateById(menu);
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        AdminMenu menu = getMenuOrThrow(menuId);
        if (menu.getIsSystem() != null && menu.getIsSystem() == 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "系统菜单不能删除");
        }
        long childCount = adminMenuMapper.selectCount(new LambdaQueryWrapper<AdminMenu>()
            .eq(AdminMenu::getParentId, menuId));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "请先删除子菜单");
        }
        long bindingCount = adminRoleMenuMapper.selectCount(new LambdaQueryWrapper<AdminRoleMenu>()
            .eq(AdminRoleMenu::getMenuId, menuId));
        if (bindingCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该菜单已被角色引用，不能删除");
        }
        adminMenuMapper.deleteById(menuId);
    }

    public List<AdminRoleDetailResponse> listRoleDetails() {
        List<AdminRole> roles = adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
            .orderByDesc(AdminRole::getIsSystem)
            .orderByAsc(AdminRole::getCreatedAt));
        Map<Long, List<Long>> menuIdsByRoleId = adminRoleMenuMapper.selectList(new LambdaQueryWrapper<AdminRoleMenu>())
            .stream()
            .collect(Collectors.groupingBy(
                AdminRoleMenu::getRoleId,
                Collectors.mapping(AdminRoleMenu::getMenuId, Collectors.toList())
            ));

        return roles.stream().map(role -> new AdminRoleDetailResponse(
            role.getId(),
            role.getRoleCode(),
            role.getRoleName(),
            role.getStatus(),
            role.getRemark(),
            role.getIsSystem(),
            role.getCreatedAt(),
            menuIdsByRoleId.getOrDefault(role.getId(), List.of())
        )).toList();
    }

    public List<AdminRoleListItemResponse> listRoleItems() {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                .eq(AdminRole::getStatus, 1)
                .orderByDesc(AdminRole::getIsSystem)
                .orderByAsc(AdminRole::getCreatedAt))
            .stream()
            .map(role -> new AdminRoleListItemResponse(role.getId(), role.getRoleCode(), role.getRoleName()))
            .toList();
    }

    @Transactional
    public void createRole(AdminRoleSaveRequest request) {
        ensureRoleCodeUnique(request.roleCode(), null);
        AdminRole role = new AdminRole();
        role.setRoleCode(normalizeRoleCode(request.roleCode()));
        role.setRoleName(request.roleName().trim());
        role.setRemark(trimToEmpty(request.remark()));
        role.setStatus(request.status());
        role.setIsSystem(0);
        adminRoleMapper.insert(role);
    }

    @Transactional
    public void updateRole(Long roleId, AdminRoleSaveRequest request) {
        AdminRole role = getRoleOrThrow(roleId);
        String nextRoleCode = normalizeRoleCode(request.roleCode());
        if (role.getIsSystem() != null && role.getIsSystem() == 1 && !nextRoleCode.equalsIgnoreCase(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "系统角色编码不能修改");
        }
        if (AdminRoleCodes.SUPER_ADMIN.equalsIgnoreCase(role.getRoleCode()) && request.status() != null && request.status() == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "SUPER_ADMIN 不能禁用");
        }
        ensureRoleCodeUnique(nextRoleCode, roleId);
        role.setRoleCode(nextRoleCode);
        role.setRoleName(request.roleName().trim());
        role.setRemark(trimToEmpty(request.remark()));
        role.setStatus(request.status());
        adminRoleMapper.updateById(role);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        AdminRole role = getRoleOrThrow(roleId);
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "系统角色不能删除");
        }
        long bindingCount = adminUserRoleMapper.selectCount(new LambdaQueryWrapper<AdminUserRole>()
            .eq(AdminUserRole::getRoleId, roleId));
        if (bindingCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该角色已绑定管理员，不能删除");
        }
        adminRoleMenuMapper.delete(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getRoleId, roleId));
        adminRoleMapper.deleteById(roleId);
    }

    @Transactional
    public void assignRoleMenus(Long roleId, AdminAssignRoleMenusRequest request) {
        AdminRole role = getRoleOrThrow(roleId);
        if (AdminRoleCodes.SUPER_ADMIN.equalsIgnoreCase(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "SUPER_ADMIN 默认拥有全部启用菜单，无需单独分配");
        }

        List<Long> normalizedMenuIds = normalizeAssignedMenuIds(request.menuIds());
        adminRoleMenuMapper.delete(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getRoleId, roleId));
        normalizedMenuIds.forEach(menuId -> {
            AdminRoleMenu roleMenu = new AdminRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            adminRoleMenuMapper.insert(roleMenu);
        });
    }

    public PageResponse<AdminAdminUserRoleItemResponse> listAdminUsers(int pageNo, int pageSize, String keyword) {
        LambdaQueryWrapper<AdminAccount> wrapper = new LambdaQueryWrapper<AdminAccount>()
            .orderByDesc(AdminAccount::getCreatedAt);

        applyUserKeywordFilter(wrapper, keyword);

        Page<AdminAccount> page = adminAccountMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<Long> userIds = page.getRecords().stream().map(AdminAccount::getId).toList();
        Map<Long, List<AdminRole>> rolesByUserId = adminPermissionService.mapRolesByUserIds(userIds, false);

        List<AdminAdminUserRoleItemResponse> items = page.getRecords().stream().map(account -> {
            List<AdminRole> roles = rolesByUserId.getOrDefault(account.getId(), List.of());
            return new AdminAdminUserRoleItemResponse(
                account.getId(),
                account.getUsername(),
                account.getDisplayName() != null && !account.getDisplayName().isBlank() ? account.getDisplayName() : account.getUsername(),
                account.getStatus(),
                LifeRole.ADMIN.name(),
                account.getCreatedAt(),
                roles.stream().map(AdminRole::getId).toList(),
                roles.stream().map(role -> new AdminRoleListItemResponse(role.getId(), role.getRoleCode(), role.getRoleName())).toList()
            );
        }).toList();
        return new PageResponse<>(items, page.getTotal(), pageNo, pageSize);
    }

    @Transactional
    public void assignAdminUserRoles(Long userId, AdminAssignAdminUserRolesRequest request) {
        AdminAccount account = adminAccountMapper.selectById(userId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "管理员账号不存在");
        }

        List<Long> roleIds = request.roleIds() == null ? List.of() : request.roleIds().stream().distinct().toList();
        if (!roleIds.isEmpty()) {
            long count = adminRoleMapper.selectCount(new LambdaQueryWrapper<AdminRole>().in(AdminRole::getId, roleIds));
            if (count != roleIds.size()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "存在无效角色");
            }
        }

        adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getUserId, userId));
        roleIds.forEach(roleId -> {
            AdminUserRole userRole = new AdminUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            adminUserRoleMapper.insert(userRole);
        });
    }

    private void applyMenuRequest(AdminMenu menu, AdminMenuSaveRequest request, Long currentMenuId) {
        AdminMenuType menuType = AdminMenuType.fromCode(request.menuType());
        Long parentId = request.parentId() == null ? 0L : request.parentId();

        if (currentMenuId != null && currentMenuId.equals(parentId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "菜单不能选择自己作为父节点");
        }
        if (parentId != 0L) {
            AdminMenu parent = adminMenuMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "父级菜单不存在");
            }
            if (!AdminMenuType.DIRECTORY.name().equalsIgnoreCase(parent.getMenuType())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "只有目录类型菜单才能挂载子菜单");
            }
        }

        ensureRouteNameUnique(request.routeName(), currentMenuId);
        if (menuType == AdminMenuType.PAGE) {
            if (!StringUtils.hasText(request.viewKey()) || !StringUtils.hasText(request.permissionCode())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "页面菜单必须配置 viewKey 和 permissionCode");
            }
            ensurePermissionCodeUnique(request.permissionCode(), currentMenuId);
        }

        menu.setParentId(parentId);
        menu.setMenuType(menuType.name());
        menu.setMenuName(request.menuName().trim());
        menu.setRouteName(request.routeName().trim());
        menu.setRoutePath(request.routePath().trim());
        menu.setViewKey(menuType == AdminMenuType.PAGE ? request.viewKey().trim() : null);
        menu.setIconName(trimToEmpty(request.iconName()));
        menu.setPermissionCode(menuType == AdminMenuType.PAGE ? request.permissionCode().trim() : null);
        menu.setSortOrder(request.sortOrder());
        menu.setVisible(request.visible());
        menu.setStatus(request.status());
    }

    private void ensureRouteNameUnique(String routeName, Long excludeMenuId) {
        LambdaQueryWrapper<AdminMenu> wrapper = new LambdaQueryWrapper<AdminMenu>()
            .eq(AdminMenu::getRouteName, routeName.trim());
        if (excludeMenuId != null) {
            wrapper.ne(AdminMenu::getId, excludeMenuId);
        }
        if (adminMenuMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "路由名称已存在");
        }
    }

    private void ensurePermissionCodeUnique(String permissionCode, Long excludeMenuId) {
        LambdaQueryWrapper<AdminMenu> wrapper = new LambdaQueryWrapper<AdminMenu>()
            .eq(AdminMenu::getPermissionCode, permissionCode.trim())
            .eq(AdminMenu::getMenuType, AdminMenuType.PAGE.name());
        if (excludeMenuId != null) {
            wrapper.ne(AdminMenu::getId, excludeMenuId);
        }
        if (adminMenuMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "权限标识已存在");
        }
    }

    private void ensureRoleCodeUnique(String roleCode, Long excludeRoleId) {
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<AdminRole>()
            .eq(AdminRole::getRoleCode, normalizeRoleCode(roleCode));
        if (excludeRoleId != null) {
            wrapper.ne(AdminRole::getId, excludeRoleId);
        }
        if (adminRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
    }

    private List<Long> normalizeAssignedMenuIds(List<Long> rawMenuIds) {
        if (rawMenuIds == null || rawMenuIds.isEmpty()) {
            return List.of();
        }
        List<AdminMenu> allMenus = adminPermissionService.listAllMenus();
        Map<Long, AdminMenu> menuMap = allMenus.stream().collect(Collectors.toMap(AdminMenu::getId, Function.identity()));

        Set<Long> normalizedIds = new LinkedHashSet<>();
        for (Long menuId : rawMenuIds) {
            if (!menuMap.containsKey(menuId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "存在无效菜单");
            }

            Long cursor = menuId;
            while (cursor != null && cursor != 0L) {
                AdminMenu cursorMenu = menuMap.get(cursor);
                if (cursorMenu == null) {
                    break;
                }
                normalizedIds.add(cursorMenu.getId());
                cursor = cursorMenu.getParentId();
            }
        }
        return new ArrayList<>(normalizedIds);
    }

    private void applyUserKeywordFilter(LambdaQueryWrapper<AdminAccount> wrapper, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String normalizedKeyword = keyword.trim();
        // 后台账号与 C 端资料彻底拆开后，管理员搜索只在 admin_account 自己的用户名和展示名字段里完成。
        wrapper.and(nested -> nested
            .like(AdminAccount::getUsername, normalizedKeyword)
            .or()
            .like(AdminAccount::getDisplayName, normalizedKeyword));
    }

    private AdminMenu getMenuOrThrow(Long menuId) {
        AdminMenu menu = adminMenuMapper.selectById(menuId);
        if (menu == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜单不存在");
        }
        return menu;
    }

    private AdminRole getRoleOrThrow(Long roleId) {
        AdminRole role = adminRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private String normalizeRoleCode(String roleCode) {
        return roleCode.trim().toUpperCase();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
