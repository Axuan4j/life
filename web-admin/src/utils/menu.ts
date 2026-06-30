import type { RouteRecordRaw, Router } from 'vue-router';
import type { AdminMenuNode, AdminSessionResponse } from '../types/admin';
import { viewRegistry } from '../router/viewRegistry';

export interface DynamicRouteMeta {
  title: string;
  permission?: string;
  menuId: number;
  viewKey?: string;
}

function toChildPath(routePath: string) {
  return routePath.replace(/^\/+/, '');
}

function flattenPageMenus(menus: AdminMenuNode[]): AdminMenuNode[] {
  return menus.flatMap((item) => {
    if (item.menuType === 'PAGE') {
      return [item];
    }
    return flattenPageMenus(item.children);
  });
}

export function buildDynamicRoutes(menus: AdminMenuNode[]): RouteRecordRaw[] {
  return flattenPageMenus(menus).map((menu) => {
    const loadView = menu.viewKey ? viewRegistry[menu.viewKey] : undefined;
    if (!loadView) {
      throw new Error(`未找到 viewKey 对应页面: ${menu.viewKey}`);
    }
    return {
      path: toChildPath(menu.routePath),
      name: menu.routeName,
      component: async () => await loadView(),
      meta: {
        title: menu.menuName,
        permission: menu.permissionCode ?? undefined,
        menuId: menu.id,
        viewKey: menu.viewKey ?? undefined,
      } satisfies DynamicRouteMeta,
    };
  });
}

export function collectDynamicRouteNames(menus: AdminMenuNode[]) {
  return flattenPageMenus(menus).map((menu) => menu.routeName);
}

export function registerDynamicRoutes(router: Router, menus: AdminMenuNode[]) {
  const routes = buildDynamicRoutes(menus);
  routes.forEach((route) => {
    if (route.name && !router.hasRoute(route.name)) {
      // 所有后台业务页都挂到固定布局壳下面，菜单变化时只替换子路由，避免整体路由树失控。
      router.addRoute('admin-root', route);
    }
  });
  return collectDynamicRouteNames(menus);
}

export function resolveHomePath(session: AdminSessionResponse) {
  return session.homePath || '/403';
}
