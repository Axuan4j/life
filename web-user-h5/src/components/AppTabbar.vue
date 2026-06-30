<template>
  <nav class="tabbar">
    <button
      v-for="item in tabs"
      :key="item.to"
      type="button"
      class="tab-item"
      :class="{ active: isActive(item.to) }"
      @click="go(item.to)"
    >
      <span class="icon-wrap">
        <van-badge
          v-if="item.to === '/message'"
          :content="messageUnreadCount"
          :max="99"
          :show-zero="false"
          class="message-badge"
        >
          <span class="icon-shell">
            <AppTabIcon :name="item.icon" class="icon" />
          </span>
        </van-badge>
        <span v-else class="icon-shell">
          <AppTabIcon :name="item.icon" class="icon" />
        </span>
      </span>
      <span class="label">{{ item.label }}</span>
    </button>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserNotificationStore } from '../stores/notification';
import AppTabIcon from './AppTabIcon.vue';

const route = useRoute();
const router = useRouter();
const notificationStore = useUserNotificationStore();

type TabIconName = 'home' | 'discover' | 'compose' | 'message' | 'profile';

const tabs: Array<{ label: string; to: string; icon: TabIconName }> = [
  { label: '首页', to: '/', icon: 'home' },
  { label: '发现', to: '/discover', icon: 'discover' },
  { label: '发帖', to: '/compose', icon: 'compose' },
  { label: '消息', to: '/message', icon: 'message' },
  { label: '我的', to: '/profile', icon: 'profile' },
];

const messageUnreadCount = computed(() => notificationStore.unreadCount);

function isActive(path: string) {
  return route.path === path;
}

function go(path: string) {
  // 这里统一由底部导航做路由跳转，方便后面把激活态、动效和埋点收口在一个组件内。
  if (route.path !== path) {
    router.push(path);
  }
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  left: 12px;
  right: 12px;
  bottom: 10px;
  z-index: 30;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: end;
  padding: 10px 8px calc(10px + env(safe-area-inset-bottom));
  border: 1px solid rgba(238, 240, 244, 0.92);
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.92) 100%);
  box-shadow:
    0 18px 40px rgba(26, 36, 56, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(16px);
}

.tab-item {
  border: none;
  background: transparent;
  color: #94a3b8;
  font: inherit;
}

.tab-item {
  display: grid;
  justify-items: center;
  gap: 6px;
  padding: 0;
  transition: color 0.2s ease;
}

.icon-wrap {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  transition: transform 0.22s ease, box-shadow 0.22s ease, background 0.22s ease, color 0.22s ease;
}

.icon-shell {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  border-radius: inherit;
  background: rgba(248, 250, 252, 0.25);
}

.message-badge {
  display: inline-flex;
}

.message-badge :deep(.van-badge__wrapper) {
  display: inline-flex;
}

.icon {
  font-size: 22px;
  line-height: 1;
}

.label {
  font-size: 11px;
  line-height: 1;
  font-weight: 500;
  transition: transform 0.22s ease, color 0.22s ease;
}

.tab-item.active {
  color: var(--lf-color-text-primary);
}

.tab-item.active .icon-wrap {
  transform: translateY(-18px);
  background: linear-gradient(135deg, #ff7a59 0%, #ff4a26 100%);
  color: #fff;
  box-shadow: 0 16px 28px rgba(255, 95, 61, 0.32);
  animation: tab-pop 0.24s ease;
}

.tab-item.active .icon-shell {
  background: transparent;
}

.tab-item.active .label {
  transform: translateY(-4px);
  color: var(--lf-color-primary);
}

.tab-item:not(.active):hover .icon-wrap {
  transform: translateY(-4px);
}

@keyframes tab-pop {
  0% {
    transform: translateY(-10px) scale(0.92);
  }
  70% {
    transform: translateY(-20px) scale(1.04);
  }
  100% {
    transform: translateY(-18px) scale(1);
  }
}
</style>
