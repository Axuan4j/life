package com.xuan.life.admin.service;

import com.xuan.life.admin.entity.AdminMenu;
import com.xuan.life.admin.entity.AdminRole;
import com.xuan.life.admin.entity.AdminRoleMenu;
import com.xuan.life.admin.entity.AdminUserRole;
import com.xuan.life.admin.mapper.AdminMenuMapper;
import com.xuan.life.admin.mapper.AdminRoleMapper;
import com.xuan.life.admin.mapper.AdminRoleMenuMapper;
import com.xuan.life.admin.mapper.AdminUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPermissionServiceTest {

    @Mock
    private AdminRoleMapper adminRoleMapper;
    @Mock
    private AdminMenuMapper adminMenuMapper;
    @Mock
    private AdminUserRoleMapper adminUserRoleMapper;
    @Mock
    private AdminRoleMenuMapper adminRoleMenuMapper;

    @InjectMocks
    private AdminPermissionService adminPermissionService;

    @Test
    void shouldReturnEmptyAuthoritiesForNonAdminPlatformRole() {
        Collection<String> authorities = adminPermissionService.loadAuthorities(10001L, "alice", "USER");
        assertThat(authorities).isEmpty();
    }

    @Test
    void shouldResolvePagePermissionsForAssignedRole() {
        AdminUserRole adminUserRole = new AdminUserRole();
        adminUserRole.setUserId(10001L);
        adminUserRole.setRoleId(81002L);

        AdminRole adminRole = new AdminRole();
        adminRole.setId(81002L);
        adminRole.setRoleCode("USER_ADMIN");
        adminRole.setStatus(1);

        AdminRoleMenu roleMenu = new AdminRoleMenu();
        roleMenu.setRoleId(81002L);
        roleMenu.setMenuId(82002L);

        AdminMenu adminMenu = new AdminMenu();
        adminMenu.setId(82002L);
        adminMenu.setMenuType("PAGE");
        adminMenu.setPermissionCode("admin:user");
        adminMenu.setStatus(1);
        adminMenu.setVisible(1);

        when(adminUserRoleMapper.selectList(any())).thenReturn(List.of(adminUserRole));
        when(adminRoleMapper.selectList(any())).thenReturn(List.of(adminRole));
        when(adminRoleMenuMapper.selectList(any())).thenReturn(List.of(roleMenu));
        when(adminMenuMapper.selectList(any())).thenReturn(List.of(adminMenu));

        Collection<String> authorities = adminPermissionService.loadAuthorities(10001L, "admin", "ADMIN");

        assertThat(authorities).containsExactly("admin:user");
    }
}
