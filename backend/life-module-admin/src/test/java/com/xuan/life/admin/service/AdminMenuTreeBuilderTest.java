package com.xuan.life.admin.service;

import com.xuan.life.admin.entity.AdminMenu;
import com.xuan.life.admin.web.response.AdminMenuTreeItemResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminMenuTreeBuilderTest {

    private final AdminMenuTreeBuilder adminMenuTreeBuilder = new AdminMenuTreeBuilder();

    @Test
    void shouldBuildTreeAndKeepSortOrder() {
        AdminMenu rootDirectory = menu(1L, 0L, "DIRECTORY", "权限管理", 20);
        AdminMenu overviewPage = menu(2L, 0L, "PAGE", "概览", 10);
        AdminMenu rolePage = menu(3L, 1L, "PAGE", "角色管理", 30);
        AdminMenu menuPage = menu(4L, 1L, "PAGE", "菜单管理", 10);

        List<AdminMenuTreeItemResponse> tree = adminMenuTreeBuilder.buildTree(
            List.of(rootDirectory, overviewPage, rolePage, menuPage)
        );

        assertThat(tree).hasSize(2);
        assertThat(tree.get(0).menuName()).isEqualTo("概览");
        assertThat(tree.get(1).menuName()).isEqualTo("权限管理");
        assertThat(tree.get(1).children()).extracting(AdminMenuTreeItemResponse::menuName)
            .containsExactly("菜单管理", "角色管理");
    }

    private AdminMenu menu(Long id, Long parentId, String type, String name, Integer sortOrder) {
        AdminMenu menu = new AdminMenu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuType(type);
        menu.setMenuName(name);
        menu.setRouteName(name + "-route");
        menu.setRoutePath("/" + id);
        menu.setSortOrder(sortOrder);
        menu.setVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        return menu;
    }
}
