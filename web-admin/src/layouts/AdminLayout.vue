<template>
  <n-layout has-sider class="admin-shell">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed="shellStore.sidebarCollapsed"
      :collapsed-width="72"
      :width="248"
      class="admin-shell__sider"
    >
      <div class="admin-logo-card">
        <div class="admin-logo-mark">L</div>
        <div v-show="!shellStore.sidebarCollapsed" class="admin-logo-copy">
          <strong>Life 后台</strong>
          <span>管理中心</span>
        </div>
      </div>

      <n-scrollbar class="admin-menu-scroll">
        <n-menu
          v-model:expanded-keys="expandedKeys"
          :value="activeMenuKey"
          :collapsed="shellStore.sidebarCollapsed"
          :collapsed-width="72"
          :collapsed-icon-size="20"
          :options="menuOptions"
          accordion
          class="admin-menu"
          @update:value="handleMenuSelect"
        />
      </n-scrollbar>
    </n-layout-sider>

    <n-layout class="admin-shell__main">
      <header class="admin-topbar">
        <div class="admin-topbar__left">
          <n-button quaternary circle @click="shellStore.toggleSidebarCollapsed()">
            <template #icon>
              <n-icon :component="shellStore.sidebarCollapsed ? MenuOutline : MenuSharp" />
            </template>
          </n-button>

          <div class="admin-topbar__copy">
            <strong>{{ currentTitle }}</strong>
            <n-breadcrumb>
              <n-breadcrumb-item v-for="item in breadcrumbs" :key="item.key">
                {{ item.label }}
              </n-breadcrumb-item>
            </n-breadcrumb>
          </div>
        </div>

        <div class="admin-topbar__right">
          <div class="admin-operator-chip">
            <n-avatar round :size="38">
              {{ operatorInitial }}
            </n-avatar>
            <div class="admin-operator-chip__copy">
              <strong>{{ operatorLabel }}</strong>
              <span>{{ roleSummary }}</span>
            </div>
          </div>

          <n-tooltip>
            <template #trigger>
              <n-button quaternary circle @click="shellStore.toggleThemeMode()">
                <template #icon>
                  <n-icon :component="shellStore.isDarkMode ? SunnyOutline : MoonOutline" />
                </template>
              </n-button>
            </template>
            {{ shellStore.isDarkMode ? '切换浅色模式' : '切换深色模式' }}
          </n-tooltip>

          <n-button secondary type="primary" @click="handleLogout">退出登录</n-button>
        </div>
      </header>

      <div v-if="routeTabs.length > 0" class="admin-tabs-card">
        <n-tabs
          :value="activeTabPath"
          type="card"
          closable
          size="small"
          @update:value="handleTabChange"
          @close="handleTabRemove"
        >
          <n-tab-pane
            v-for="tab in routeTabs"
            :key="tab.routePath"
            :name="tab.routePath"
            :tab="tab.title"
            :closable="tab.closable"
          />
        </n-tabs>
      </div>

      <n-layout-content class="admin-content">
        <div class="admin-content__inner">
          <router-view v-slot="{ Component, route: currentRoute }">
            <keep-alive :include="cachedViewNames">
              <component :is="Component" v-if="Component" :key="currentRoute.fullPath" />
            </keep-alive>
          </router-view>
        </div>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter, type RouteLocationNormalizedLoaded } from 'vue-router';
import {
  NAvatar,
  NBreadcrumb,
  NBreadcrumbItem,
  NButton,
  NIcon,
  NLayout,
  NLayoutContent,
  NLayoutSider,
  NMenu,
  NScrollbar,
  NTabPane,
  NTabs,
  NTooltip,
  type MenuOption,
} from 'naive-ui';
import { MenuOutline, MenuSharp, MoonOutline, SunnyOutline } from '@vicons/ionicons5';
import { logout } from '../services/api';
import { useAdminAuthStore } from '../stores/auth';
import { useAdminPermissionStore } from '../stores/permission';
import { useAdminShellStore } from '../stores/shell';
import type { AdminMenuNode } from '../types/admin';
import { renderMenuIcon } from '../utils/iconRegistry';

interface BreadcrumbItem {
  key: string;
  label: string;
}

const route = useRoute();
const router = useRouter();
const authStore = useAdminAuthStore();
const permissionStore = useAdminPermissionStore();
const shellStore = useAdminShellStore();

const expandedKeys = ref<string[]>([]);

const activeMenuKey = computed(() => route.path);
const routeTabs = computed(() => shellStore.tabs);
const activeTabPath = computed(() => shellStore.activeTabPath);
const cachedViewNames = computed(() => shellStore.cachedViewNames);
const operatorLabel = computed(() => authStore.operator?.nickname || authStore.operator?.username || '管理员');
const operatorInitial = computed(() => operatorLabel.value.trim().slice(0, 1).toUpperCase());
const roleSummary = computed(() => authStore.operator?.adminRoleNames.join(' / ') || '未分配角色');

const menuOptions = computed<MenuOption[]>(() => permissionStore.menus.filter(isVisibleMenu).map(mapMenuOption));
const breadcrumbs = computed(() => buildBreadcrumbs(route, permissionStore.menus));
const currentTitle = computed(() => breadcrumbs.value[breadcrumbs.value.length - 1]?.label ?? '首页');

watch(
  () => route.fullPath,
  () => {
    shellStore.syncRoute(route, permissionStore.homePath);
    expandedKeys.value = collectExpandedKeys(route.path, permissionStore.menus);
  },
  { immediate: true },
);

watch(
  [() => permissionStore.menus, () => permissionStore.homePath],
  () => {
    const fallbackPath = shellStore.pruneTabs(permissionStore.menus, permissionStore.homePath);
    if (fallbackPath && fallbackPath !== route.path) {
      router.replace(fallbackPath);
      return;
    }
    expandedKeys.value = collectExpandedKeys(route.path, permissionStore.menus);
  },
  { deep: true, immediate: true },
);

function isVisibleMenu(item: AdminMenuNode) {
  return item.visible === 1 && item.status === 1;
}

function mapMenuOption(item: AdminMenuNode): MenuOption {
  const visibleChildren = item.children.filter(isVisibleMenu);
  return {
    key: item.menuType === 'PAGE' ? item.routePath : `dir-${item.id}`,
    label: item.menuName,
    icon: renderMenuIcon(item.iconName),
    children: visibleChildren.length ? visibleChildren.map(mapMenuOption) : undefined,
    disabled: item.menuType !== 'PAGE' && !visibleChildren.length,
    routePath: item.routePath,
  } as MenuOption;
}

function collectExpandedKeys(currentPath: string, items: AdminMenuNode[], trail: string[] = []): string[] {
  for (const item of items) {
    if (!isVisibleMenu(item)) {
      continue;
    }

    const nextTrail = item.menuType === 'DIRECTORY' ? [...trail, `dir-${item.id}`] : trail;
    if (item.menuType === 'PAGE' && item.routePath === currentPath) {
      return nextTrail;
    }

    if (item.children.length) {
      const found = collectExpandedKeys(currentPath, item.children, nextTrail);
      if (found.length) {
        return found;
      }
    }
  }
  return [];
}

function findMenuTrail(currentPath: string, items: AdminMenuNode[], parents: AdminMenuNode[] = []): AdminMenuNode[] {
  for (const item of items) {
    if (!isVisibleMenu(item)) {
      continue;
    }

    if (item.menuType === 'PAGE' && item.routePath === currentPath) {
      return [...parents, item];
    }

    if (item.children.length) {
      const found = findMenuTrail(currentPath, item.children, [...parents, item]);
      if (found.length) {
        return found;
      }
    }
  }
  return [];
}

function buildBreadcrumbs(currentRoute: RouteLocationNormalizedLoaded, menus: AdminMenuNode[]): BreadcrumbItem[] {
  const menuTrail = findMenuTrail(currentRoute.path, menus);
  if (menuTrail.length) {
    return menuTrail.map((item) => ({
      key: String(item.id),
      label: item.menuName,
    }));
  }

  if (typeof currentRoute.meta.title === 'string') {
    return [
      {
        key: currentRoute.path,
        label: currentRoute.meta.title,
      },
    ];
  }

  return [];
}

async function handleMenuSelect(value: string) {
  if (!value.startsWith('/')) {
    return;
  }
  await router.push(value);
}

async function handleTabChange(value: string) {
  await router.push(value);
}

async function handleTabRemove(routePath: string) {
  const nextPath = shellStore.closeTab(routePath, permissionStore.homePath);
  if (nextPath && nextPath !== route.path) {
    await router.push(nextPath);
  }
}

async function handleLogout() {
  try {
    await logout();
  } catch {
    // 登出失败不阻断前端清理，本地 token 被清空后仍可回到登录页。
  }
  permissionStore.reset(router);
  authStore.clearSession();
  shellStore.reset();
  await router.replace('/login');
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
}

.admin-shell__sider {
  background: rgba(255, 255, 255, 0.88);
}

.admin-logo-card {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 72px;
  padding: 0 18px;
}

.admin-logo-mark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 14px;
  background: linear-gradient(135deg, #4f7cff 0%, #76a4ff 100%);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  box-shadow: 0 10px 24px rgba(79, 124, 255, 0.24);
}

.admin-logo-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-logo-copy strong {
  color: var(--life-text);
  font-size: 17px;
  font-weight: 700;
}

.admin-logo-copy span {
  margin-top: 4px;
  color: var(--life-text-soft);
  font-size: 12px;
}

.admin-menu-scroll {
  height: calc(100vh - 72px);
  padding: 8px 12px 18px;
}

.admin-menu :deep(.n-menu-item-content),
.admin-menu :deep(.n-submenu-children .n-menu-item-content),
.admin-menu :deep(.n-submenu-children .n-submenu-children-header) {
  font-size: 14px;
}

.admin-menu :deep(.n-menu-item-content.n-menu-item-content--selected),
.admin-menu :deep(.n-menu-item-content.n-menu-item-content--selected:hover) {
  background: linear-gradient(135deg, rgba(79, 124, 255, 0.14) 0%, rgba(121, 169, 255, 0.18) 100%);
  color: var(--life-primary);
}

.admin-shell__main {
  background: transparent;
}

.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin: 18px 18px 0;
  padding: 16px 18px;
  border: 1px solid var(--life-border);
  border-radius: 18px;
  background: var(--life-card);
  box-shadow: var(--life-shadow-soft);
}

.admin-topbar__left,
.admin-topbar__right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.admin-topbar__copy {
  min-width: 0;
}

.admin-topbar__copy strong {
  display: block;
  color: var(--life-text);
  font-size: 20px;
  font-weight: 700;
}

.admin-topbar__copy :deep(.n-breadcrumb) {
  margin-top: 6px;
}

.admin-operator-chip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border: 1px solid var(--life-border);
  border-radius: 14px;
  background: var(--life-bg-soft);
}

.admin-operator-chip__copy {
  display: flex;
  flex-direction: column;
}

.admin-operator-chip__copy strong {
  color: var(--life-text);
  font-size: 14px;
}

.admin-operator-chip__copy span {
  margin-top: 4px;
  color: var(--life-text-soft);
  font-size: 12px;
}

.admin-tabs-card {
  margin: 14px 18px 0;
  padding: 0 12px;
  border: 1px solid var(--life-border);
  border-radius: 16px;
  background: var(--life-card);
  box-shadow: var(--life-shadow-soft);
}

.admin-tabs-card :deep(.n-tabs-nav) {
  padding-top: 10px;
}

.admin-tabs-card :deep(.n-tabs-tab) {
  border-radius: 12px 12px 0 0;
}

.admin-content {
  min-height: 0;
}

.admin-content__inner {
  padding: 18px;
}

@media (max-width: 1080px) {
  .admin-topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .admin-topbar__right {
    justify-content: space-between;
    flex-wrap: wrap;
  }
}
</style>
