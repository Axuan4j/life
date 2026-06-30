import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import type { Router } from 'vue-router';
import { fetchSession } from '../services/api';
import { useAdminAuthStore } from './auth';
import type { AdminMenuNode, AdminSessionResponse } from '../types/admin';
import { registerDynamicRoutes, resolveHomePath } from '../utils/menu';

export const useAdminPermissionStore = defineStore('admin-permission', () => {
  const menus = ref<AdminMenuNode[]>([]);
  const permissions = ref<string[]>([]);
  const homePath = ref('/403');
  const initialized = ref(false);
  const dynamicRouteNames = ref<string[]>([]);

  const permissionSet = computed(() => new Set(permissions.value));

  function clearRoutes(router: Router) {
    dynamicRouteNames.value.forEach((name) => {
      if (router.hasRoute(name)) {
        router.removeRoute(name);
      }
    });
    dynamicRouteNames.value = [];
  }

  function reset(router: Router) {
    clearRoutes(router);
    menus.value = [];
    permissions.value = [];
    homePath.value = '/403';
    initialized.value = false;
  }

  function applySession(router: Router, session: AdminSessionResponse) {
    // 角色或菜单一旦变化，直接按最新 session 重建动态路由，保证刷新后和下一次请求的权限视图一致。
    clearRoutes(router);
    menus.value = session.menus;
    permissions.value = session.permissions;
    homePath.value = resolveHomePath(session);
    dynamicRouteNames.value = registerDynamicRoutes(router, session.menus);
    initialized.value = true;
  }

  async function bootstrap(router: Router) {
    const authStore = useAdminAuthStore();
    if (!authStore.accessToken) {
      reset(router);
      return null;
    }
    const session = await fetchSession();
    authStore.setOperator(session.operator);
    applySession(router, session);
    return session;
  }

  function hasPermission(permission?: string) {
    if (!permission) {
      return true;
    }
    return permissionSet.value.has(permission);
  }

  return {
    menus,
    permissions,
    homePath,
    initialized,
    dynamicRouteNames,
    bootstrap,
    applySession,
    hasPermission,
    reset,
  };
});
