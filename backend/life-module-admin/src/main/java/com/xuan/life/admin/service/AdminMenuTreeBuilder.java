package com.xuan.life.admin.service;

import com.xuan.life.admin.entity.AdminMenu;
import com.xuan.life.admin.web.response.AdminMenuTreeItemResponse;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AdminMenuTreeBuilder {

    private static final Long ROOT_PARENT_ID = 0L;

    private static final Comparator<AdminMenu> MENU_COMPARATOR = Comparator
        .comparing((AdminMenu menu) -> menu.getSortOrder() == null ? Integer.MAX_VALUE : menu.getSortOrder())
        .thenComparing(menu -> menu.getId() == null ? 0L : menu.getId());

    public List<AdminMenuTreeItemResponse> buildTree(Collection<AdminMenu> menus) {
        Map<Long, List<AdminMenu>> childrenMap = menus.stream()
            .sorted(MENU_COMPARATOR)
            .collect(Collectors.groupingBy(menu -> normalizeParentId(menu.getParentId())));
        return buildChildren(ROOT_PARENT_ID, childrenMap);
    }

    private List<AdminMenuTreeItemResponse> buildChildren(Long parentId, Map<Long, List<AdminMenu>> childrenMap) {
        return childrenMap.getOrDefault(normalizeParentId(parentId), List.of()).stream()
            // 菜单树在后端统一组装，前端只消费稳定的树结构，避免每个端都重复写一遍“找父子节点”的逻辑。
            .map(menu -> new AdminMenuTreeItemResponse(
                menu.getId(),
                normalizeParentId(menu.getParentId()),
                menu.getMenuType(),
                menu.getMenuName(),
                menu.getRouteName(),
                menu.getRoutePath(),
                menu.getViewKey(),
                menu.getIconName(),
                menu.getPermissionCode(),
                menu.getSortOrder(),
                menu.getVisible(),
                menu.getStatus(),
                menu.getIsSystem(),
                buildChildren(menu.getId(), childrenMap)
            ))
            .toList();
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }
}
