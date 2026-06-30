<template>
  <router-view />
  <AppTabbar v-if="showTabbar" />
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute } from 'vue-router';
import AppTabbar from './components/AppTabbar.vue';
import { useUserAuthStore } from './stores/auth';
import { useUserNotificationStore } from './stores/notification';

const route = useRoute();
const authStore = useUserAuthStore();
const notificationStore = useUserNotificationStore();
const showTabbar = computed(() => route.meta.showTabbar === true);

watch(
  () => authStore.accessToken,
  async (accessToken) => {
    if (!accessToken) {
      notificationStore.reset();
      return;
    }

    if (!authStore.currentUser) {
      try {
        await authStore.fetchCurrentUser();
      } catch {
        return;
      }
    }

    try {
      await notificationStore.fetchUnreadCount();
      notificationStore.connectStream();
    } catch {
      // 消息通道异常时不阻断主流程，用户仍然可以继续浏览首页和详情页。
    }
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  notificationStore.disconnectStream();
});
</script>
