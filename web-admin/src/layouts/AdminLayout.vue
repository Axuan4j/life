<template>
  <t-layout class="layout-shell">
    <t-aside class="layout-aside">
      <div class="brand-panel">
        <strong>Life Admin</strong>
        <p>内容、用户、权限和广播消息统一收口在这一套后台工作台里。</p>
      </div>
      <div class="nav-panel">
        <t-menu
          :value="activeMenu"
          :theme="shellStore.isDarkMode ? 'dark' : 'light'"
          class="side-menu"
          @change="handleMenuChange"
        >
          <SideMenuBranch v-for="item in menuItems" :key="item.id" :item="item" />
        </t-menu>
      </div>
    </t-aside>
    <t-layout class="layout-main">
      <t-header class="layout-header">
        <div class="page-intro">
          <div class="page-copy">
            <strong>{{ currentTitle }}</strong>
            <t-breadcrumb>
              <t-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
                {{ item.title }}
              </t-breadcrumb-item>
            </t-breadcrumb>
          </div>
        </div>
        <div class="header-right">
          <div class="operator-chip">
            <span class="operator-name">{{ operatorLabel }}</span>
            <span class="operator-role">{{ roleSummary }}</span>
          </div>
          <t-button theme="default" variant="outline" class="header-button" @click="shellStore.toggleThemeMode()">
            {{ shellStore.isDarkMode ? '浅色模式' : '深色模式' }}
          </t-button>
          <t-button theme="default" variant="outline" class="header-button" @click="handleLogout">退出登录</t-button>
        </div>
      </t-header>
      <div class="layout-tabs" v-if="routeTabs.length > 0">
        <t-tabs :value="activeTabPath" class="route-tabs" theme="card" @change="handleTabChange" @remove="handleTabRemove">
          <t-tab-panel
            v-for="tab in routeTabs"
            :key="tab.routePath"
            :value="tab.routePath"
            :label="tab.title"
            :removable="tab.closable"
          />
        </t-tabs>
      </div>
      <t-content class="layout-content">
        <div class="content-shell">
          <router-view v-slot="{ Component, route: currentRoute }">
            <!-- 打开的业务页放进 keep-alive，标签切换时保留筛选条件和列表滚动，不用每次重查。 -->
            <keep-alive :include="cachedViewNames">
              <component :is="Component" v-if="Component" :key="currentRoute.fullPath" />
            </keep-alive>
          </router-view>
        </div>
      </t-content>
    </t-layout>
  </t-layout>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import SideMenuBranch from '../components/layout/SideMenuBranch.vue';
import { logout } from '../services/api';
import { useAdminAuthStore } from '../stores/auth';
import { useAdminPermissionStore } from '../stores/permission';
import { useAdminShellStore } from '../stores/shell';

const route = useRoute();
const router = useRouter();
const authStore = useAdminAuthStore();
const permissionStore = useAdminPermissionStore();
const shellStore = useAdminShellStore();

const menuItems = computed(() => permissionStore.menus.filter((item) => item.visible === 1 && item.status === 1));
const activeMenu = computed(() => route.path);
const routeTabs = computed(() => shellStore.tabs);
const activeTabPath = computed(() => shellStore.activeTabPath);
const cachedViewNames = computed(() => shellStore.cachedViewNames);
const operatorLabel = computed(() => authStore.operator?.nickname || authStore.operator?.username || '管理员');
const roleSummary = computed(() => authStore.operator?.adminRoleNames.join(' / ') || '未分配角色');
const currentTitle = computed(() => breadcrumbs.value[breadcrumbs.value.length - 1]?.title ?? '控制台');
const breadcrumbs = computed(() =>
  route.matched
    .filter((item) => typeof item.meta.title === 'string' && item.path !== '/')
    .map((item) => ({
      path: item.path,
      title: String(item.meta.title),
    })),
);

watch(
  () => route.fullPath,
  () => {
    shellStore.syncRoute(route, permissionStore.homePath);
  },
  { immediate: true },
);

watch(
  [() => permissionStore.menus, () => permissionStore.homePath],
  () => {
    const fallbackPath = shellStore.pruneTabs(permissionStore.menus, permissionStore.homePath);
    if (fallbackPath && fallbackPath !== route.path) {
      router.replace(fallbackPath);
    }
  },
  { deep: true, immediate: true },
);

async function handleMenuChange(value: string | number) {
  await router.push(String(value));
}

async function handleTabChange(value: string | number) {
  await router.push(String(value));
}

async function handleTabRemove(context: { value: string | number }) {
  const nextPath = shellStore.closeTab(String(context.value), permissionStore.homePath);
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
.layout-shell {
  min-height: 100vh;
  background: transparent;
  color: var(--td-text-color-primary);
}

.layout-aside {
  width: 284px;
  background: linear-gradient(180deg, rgba(255, 252, 249, 0.9) 0%, rgba(247, 250, 255, 0.94) 100%);
  border-right: 1px solid rgba(25, 35, 52, 0.08);
  padding: 22px 14px;
  backdrop-filter: blur(16px);
}

.brand-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 18px;
  padding: 18px;
  border: 1px solid rgba(25, 35, 52, 0.06);
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(255, 210, 198, 0.62) 0%, transparent 36%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(251, 245, 241, 0.96) 100%);
  box-shadow: 0 18px 36px rgba(24, 35, 52, 0.06);
}

.brand-badge {
  display: inline-flex;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(232, 109, 79, 0.12);
  color: #d25d40;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.brand-panel strong {
  color: var(--td-text-color-primary);
  font-size: 22px;
}

.brand-panel p {
  margin: 0;
  color: var(--td-text-color-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.nav-panel {
  padding: 10px;
  border: 1px solid rgba(25, 35, 52, 0.06);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 16px 34px rgba(24, 35, 52, 0.05);
}

.side-menu {
  border-radius: 18px;
  background: transparent !important;
}

.layout-main {
  min-width: 0;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 88px;
  margin: 16px 18px 0;
  padding: 18px 22px;
  border: 1px solid rgba(25, 35, 52, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px);
  box-shadow: 0 16px 34px rgba(24, 35, 52, 0.05);
}

.page-intro {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.page-kicker {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(237, 123, 89, 0.16) 0%, rgba(89, 159, 255, 0.14) 100%);
  color: #d46042;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-copy {
  min-width: 0;
}

.page-copy strong {
  display: block;
  color: var(--td-text-color-primary);
  font-size: 22px;
  font-weight: 700;
}

.page-copy :deep(.t-breadcrumb) {
  margin-top: 6px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.operator-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(244, 247, 251, 0.86);
}

.operator-name {
  color: var(--td-text-color-primary);
  font-size: 14px;
  font-weight: 600;
}

.operator-role {
  color: var(--td-text-color-secondary);
  font-size: 12px;
}

.header-button {
  min-width: 96px;
}

.layout-tabs {
  margin: 12px 18px 0;
  padding: 0 6px;
}

.layout-content {
  padding: 16px 18px 24px;
  background: transparent;
}

.content-shell {
  width: 100%;
}

.route-tabs :deep(.t-tabs__nav-item) {
  border-radius: 14px 14px 0 0;
}

.side-menu :deep(.t-menu__item),
.side-menu :deep(.t-menu__expand-icon + .t-menu__content) {
  border-radius: 14px;
}

html[theme-mode='dark'] .layout-aside,
html[theme-mode='dark'] .layout-header,
html[theme-mode='dark'] .nav-panel,
html[theme-mode='dark'] .brand-panel,
html[theme-mode='dark'] .operator-chip {
  background: rgba(18, 24, 33, 0.86);
  border-color: rgba(255, 255, 255, 0.06);
}
</style>
