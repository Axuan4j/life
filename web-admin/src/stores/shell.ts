import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import type { RouteLocationNormalizedLoaded } from 'vue-router';
import type { AdminMenuNode } from '../types/admin';

const ADMIN_THEME_MODE_KEY = 'life_admin_theme_mode';

type AdminThemeMode = 'light' | 'dark';

export interface AdminRouteTab {
  routePath: string;
  routeName: string;
  title: string;
  viewKey: string;
  closable: boolean;
}

function flattenPageMenus(menus: AdminMenuNode[]): AdminMenuNode[] {
  return menus.flatMap((menu) => {
    if (menu.menuType === 'PAGE') {
      return [menu];
    }
    return flattenPageMenus(menu.children);
  });
}

function isTabRoute(route: RouteLocationNormalizedLoaded) {
  return !route.meta.public && typeof route.meta.title === 'string' && route.path !== '/';
}

function readThemeMode(): AdminThemeMode {
  if (typeof window === 'undefined') {
    return 'light';
  }
  return localStorage.getItem(ADMIN_THEME_MODE_KEY) === 'dark' ? 'dark' : 'light';
}

export const useAdminShellStore = defineStore('admin-shell', () => {
  const tabs = ref<AdminRouteTab[]>([]);
  const activeTabPath = ref('');
  const themeMode = ref<AdminThemeMode>(readThemeMode());

  const isDarkMode = computed(() => themeMode.value === 'dark');
  const cachedViewNames = computed(() =>
    Array.from(new Set(tabs.value.map((tab) => tab.viewKey).filter((viewKey) => Boolean(viewKey)))),
  );

  function applyThemeMode(nextThemeMode: AdminThemeMode) {
    themeMode.value = nextThemeMode;
    localStorage.setItem(ADMIN_THEME_MODE_KEY, nextThemeMode);

    if (typeof document === 'undefined') {
      return;
    }

    if (nextThemeMode === 'dark') {
      document.documentElement.setAttribute('theme-mode', 'dark');
      return;
    }

    document.documentElement.removeAttribute('theme-mode');
  }

  function initializeThemeMode() {
    applyThemeMode(themeMode.value);
  }

  function toggleThemeMode() {
    applyThemeMode(isDarkMode.value ? 'light' : 'dark');
  }

  function syncRoute(route: RouteLocationNormalizedLoaded, homePath: string) {
    if (!isTabRoute(route)) {
      activeTabPath.value = route.path;
      return;
    }

    const routePath = route.path;
    const routeName = typeof route.name === 'string' ? route.name : String(route.name ?? routePath);
    const title = String(route.meta.title);
    const viewKey = typeof route.meta.viewKey === 'string' ? route.meta.viewKey : routeName;
    const closable = routePath !== homePath;

    activeTabPath.value = routePath;
    const existingTab = tabs.value.find((tab) => tab.routePath === routePath);
    if (existingTab) {
      existingTab.title = title;
      existingTab.routeName = routeName;
      existingTab.viewKey = viewKey;
      existingTab.closable = closable;
      return;
    }

    // 标签页只记录当前真正访问过的业务页，避免菜单很多时首屏就把所有页面都塞进顶部 tabs。
    tabs.value.push({
      routePath,
      routeName,
      title,
      viewKey,
      closable,
    });
  }

  function pruneTabs(menus: AdminMenuNode[], homePath: string) {
    const allowedPaths = new Set(flattenPageMenus(menus).map((menu) => menu.routePath));
    tabs.value = tabs.value
      .filter((tab) => allowedPaths.has(tab.routePath))
      .map((tab) => ({
        ...tab,
        closable: tab.routePath !== homePath,
      }));

    if (tabs.value.length === 0) {
      activeTabPath.value = '';
      return allowedPaths.has(homePath) ? homePath : null;
    }

    if (tabs.value.some((tab) => tab.routePath === activeTabPath.value)) {
      return null;
    }

    const fallbackTab = tabs.value.find((tab) => tab.routePath === homePath) ?? tabs.value[0];
    activeTabPath.value = fallbackTab.routePath;
    return fallbackTab.routePath;
  }

  function closeTab(routePath: string, homePath: string) {
    const targetIndex = tabs.value.findIndex((tab) => tab.routePath === routePath);
    if (targetIndex < 0) {
      return null;
    }

    const targetTab = tabs.value[targetIndex];
    if (!targetTab.closable) {
      activeTabPath.value = targetTab.routePath;
      return targetTab.routePath;
    }

    const closingActiveTab = activeTabPath.value === routePath;
    tabs.value.splice(targetIndex, 1);
    if (!tabs.value.length) {
      activeTabPath.value = homePath;
      return homePath;
    }

    if (!closingActiveTab) {
      return null;
    }

    const fallbackTab =
      tabs.value[targetIndex] ??
      tabs.value[targetIndex - 1] ??
      tabs.value.find((tab) => tab.routePath === homePath) ??
      tabs.value[0];
    activeTabPath.value = fallbackTab.routePath;
    return fallbackTab.routePath;
  }

  function reset() {
    tabs.value = [];
    activeTabPath.value = '';
  }

  return {
    tabs,
    activeTabPath,
    themeMode,
    isDarkMode,
    cachedViewNames,
    initializeThemeMode,
    toggleThemeMode,
    syncRoute,
    pruneTabs,
    closeTab,
    reset,
  };
});
