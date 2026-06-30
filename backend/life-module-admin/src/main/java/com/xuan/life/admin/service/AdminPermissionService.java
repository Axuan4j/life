package com.xuan.life.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.life.admin.entity.AdminMenu;
import com.xuan.life.admin.entity.AdminRole;
import com.xuan.life.admin.entity.AdminRoleMenu;
import com.xuan.life.admin.entity.AdminUserRole;
import com.xuan.life.admin.mapper.AdminMenuMapper;
import com.xuan.life.admin.mapper.AdminRoleMapper;
import com.xuan.life.admin.mapper.AdminRoleMenuMapper;
import com.xuan.life.admin.mapper.AdminUserRoleMapper;
import com.xuan.life.admin.model.AdminMenuType;
import com.xuan.life.admin.model.AdminRoleCodes;
import com.xuan.life.security.model.LifeRole;
import com.xuan.life.security.service.AdditionalAuthorityProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminPermissionService implements AdditionalAuthorityProvider {

    private final AdminRoleMapper adminRoleMapper;
    private final AdminMenuMapper adminMenuMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRoleMenuMapper adminRoleMenuMapper;

    public AdminPermissionService(
        AdminRoleMapper adminRoleMapper,
        AdminMenuMapper adminMenuMapper,
        AdminUserRoleMapper adminUserRoleMapper,
        AdminRoleMenuMapper adminRoleMenuMapper
    ) {
        this.adminRoleMapper = adminRoleMapper;
        this.adminMenuMapper = adminMenuMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminRoleMenuMapper = adminRoleMenuMapper;
    }

    @Override
    public Collection<String> loadAuthorities(Long userId, String username, String roleCode) {
        if (!LifeRole.ADMIN.name().equalsIgnoreCase(roleCode)) {
            return List.of();
        }
        return listPermissionCodes(userId);
    }

    public List<AdminRole> listRolesByUserId(Long userId, boolean onlyEnabled) {
        List<AdminUserRole> mappings = adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>()
            .eq(AdminUserRole::getUserId, userId));
        if (mappings.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = mappings.stream().map(AdminUserRole::getRoleId).distinct().toList();
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<AdminRole>()
            .in(AdminRole::getId, roleIds)
            .orderByDesc(AdminRole::getIsSystem)
            .orderByAsc(AdminRole::getCreatedAt);
        if (onlyEnabled) {
            wrapper.eq(AdminRole::getStatus, 1);
        }
        return adminRoleMapper.selectList(wrapper);
    }

    public Map<Long, List<AdminRole>> mapRolesByUserIds(Collection<Long> userIds, boolean onlyEnabled) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<AdminUserRole> mappings = adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>()
            .in(AdminUserRole::getUserId, userIds));
        if (mappings.isEmpty()) {
            return Map.of();
        }

        List<Long> roleIds = mappings.stream().map(AdminUserRole::getRoleId).distinct().toList();
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<AdminRole>().in(AdminRole::getId, roleIds);
        if (onlyEnabled) {
            wrapper.eq(AdminRole::getStatus, 1);
        }
        Map<Long, AdminRole> roleMap = adminRoleMapper.selectList(wrapper).stream()
            .collect(Collectors.toMap(AdminRole::getId, Function.identity()));

        return mappings.stream().collect(Collectors.groupingBy(
            AdminUserRole::getUserId,
            Collectors.collectingAndThen(Collectors.toList(), userRoleMappings -> userRoleMappings.stream()
                .map(mapping -> roleMap.get(mapping.getRoleId()))
                .filter(role -> role != null)
                .sorted(Comparator.comparing(AdminRole::getRoleCode))
                .toList())
        ));
    }

    public boolean isSuperAdmin(Long userId) {
        return listRolesByUserId(userId, true).stream()
            .anyMatch(role -> AdminRoleCodes.SUPER_ADMIN.equalsIgnoreCase(role.getRoleCode()));
    }

    public List<String> listPermissionCodes(Long userId) {
        List<AdminMenu> allowedMenus = listAuthorizedMenus(userId, false);
        LinkedHashSet<String> permissions = allowedMenus.stream()
            .filter(menu -> AdminMenuType.PAGE.name().equalsIgnoreCase(menu.getMenuType()))
            .map(AdminMenu::getPermissionCode)
            .filter(StringUtils::hasText)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return List.copyOf(permissions);
    }

    public List<AdminMenu> listSessionMenus(Long userId) {
        return listAuthorizedMenus(userId, true);
    }

    public List<AdminMenu> listAllMenus() {
        return adminMenuMapper.selectList(new LambdaQueryWrapper<AdminMenu>()
            .orderByAsc(AdminMenu::getSortOrder)
            .orderByAsc(AdminMenu::getId));
    }

    private List<AdminMenu> listAuthorizedMenus(Long userId, boolean onlyVisible) {
        LambdaQueryWrapper<AdminMenu> baseWrapper = new LambdaQueryWrapper<AdminMenu>()
            .eq(AdminMenu::getStatus, 1)
            .orderByAsc(AdminMenu::getSortOrder)
            .orderByAsc(AdminMenu::getId);
        if (onlyVisible) {
            baseWrapper.eq(AdminMenu::getVisible, 1);
        }

        // SUPER_ADMIN 不依赖显式角色菜单绑定，始终拿到全部启用菜单，避免把后台自己锁死。
        if (isSuperAdmin(userId)) {
            return adminMenuMapper.selectList(baseWrapper);
        }

        List<AdminRole> enabledRoles = listRolesByUserId(userId, true);
        if (enabledRoles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = enabledRoles.stream().map(AdminRole::getId).toList();
        List<Long> menuIds = adminRoleMenuMapper.selectList(new LambdaQueryWrapper<AdminRoleMenu>()
                .in(AdminRoleMenu::getRoleId, roleIds))
            .stream()
            .map(AdminRoleMenu::getMenuId)
            .distinct()
            .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        baseWrapper.in(AdminMenu::getId, menuIds);
        return adminMenuMapper.selectList(baseWrapper);
    }
}
