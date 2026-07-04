import { createRouter, createWebHistory } from 'vue-router';
import AdminLayout from '../layouts/AdminLayout.vue';
import LoginView from '../views/auth/LoginView.vue';
import ForbiddenView from '../views/error/ForbiddenView.vue';
import NotFoundView from '../views/error/NotFoundView.vue';
import { useAdminAuthStore } from '../stores/auth';
import { useAdminPermissionStore } from '../stores/permission';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'admin-root',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [],
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true, title: '登录' },
    },
    {
      path: '/403',
      name: 'forbidden',
      component: ForbiddenView,
      meta: { public: true, title: '暂无权限' },
    },
    {
      path: '/404',
      name: 'not-found',
      component: NotFoundView,
      meta: { public: true, title: '页面不存在' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'fallback-not-found',
      component: NotFoundView,
      meta: { public: true, title: '页面不存在' },
    },
  ],
});

router.beforeEach(async (to) => {
  const authStore = useAdminAuthStore();
  const permissionStore = useAdminPermissionStore();
  const isFallbackNotFound = to.name === 'fallback-not-found';

  if (to.meta.public && !isFallbackNotFound) {
    if (to.name === 'login' && authStore.isAuthenticated) {
      try {
        if (!permissionStore.initialized) {
          await permissionStore.bootstrap(router);
        }
        return permissionStore.homePath || '/403';
      } catch {
        authStore.clearSession();
        permissionStore.reset(router);
        return true;
      }
    }
    return true;
  }

  if (!authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }

  if (!permissionStore.initialized) {
    try {
      await permissionStore.bootstrap(router);
      if (isFallbackNotFound || to.matched.length === 0) {
        return { path: to.fullPath, replace: true };
      }
    } catch {
      authStore.clearSession();
      permissionStore.reset(router);
      return { name: 'login', query: { redirect: to.fullPath } };
    }
  }

  if (to.path === '/') {
    return permissionStore.homePath || '/403';
  }

  const requiredPermission = typeof to.meta.permission === 'string' ? to.meta.permission : '';
  if (requiredPermission && !permissionStore.hasPermission(requiredPermission)) {
    return { name: 'forbidden' };
  }

  return true;
});

export default router;
