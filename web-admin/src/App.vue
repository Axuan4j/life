<template>
  <n-config-provider
    :theme="naiveTheme"
    :theme-overrides="themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <n-loading-bar-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <n-message-provider placement="top-right">
            <n-global-style />
            <router-view />
          </n-message-provider>
        </n-notification-provider>
      </n-dialog-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  NConfigProvider,
  NDialogProvider,
  NGlobalStyle,
  NLoadingBarProvider,
  NMessageProvider,
  NNotificationProvider,
  darkTheme,
  dateZhCN,
  zhCN,
} from 'naive-ui';
import { useAdminShellStore } from './stores/shell';

const shellStore = useAdminShellStore();

shellStore.initializeThemeMode();

const naiveTheme = computed(() => (shellStore.isDarkMode ? darkTheme : null));

const themeOverrides = computed(() => ({
  common: {
    primaryColor: shellStore.isDarkMode ? '#7aa2ff' : '#4f7cff',
    primaryColorHover: shellStore.isDarkMode ? '#8ab0ff' : '#5f89ff',
    primaryColorPressed: shellStore.isDarkMode ? '#6a95f3' : '#456ee8',
    primaryColorSuppl: shellStore.isDarkMode ? '#7aa2ff' : '#4f7cff',
    borderRadius: '14px',
    borderRadiusSmall: '12px',
    cardColor: shellStore.isDarkMode ? 'rgba(18, 25, 38, 0.88)' : 'rgba(255, 255, 255, 0.92)',
    modalColor: shellStore.isDarkMode ? '#111827' : '#ffffff',
    popoverColor: shellStore.isDarkMode ? '#111827' : '#ffffff',
    bodyColor: 'transparent',
  },
  Card: {
    borderRadius: '20px',
    colorEmbedded: shellStore.isDarkMode ? 'rgba(18, 25, 38, 0.92)' : '#ffffff',
  },
  Input: {
    borderRadius: '14px',
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '14px',
      },
    },
  },
  Button: {
    borderRadiusMedium: '14px',
    borderRadiusSmall: '12px',
  },
  Tabs: {
    tabBorderRadiusCard: '14px',
  },
  DataTable: {
    thColor: shellStore.isDarkMode ? 'rgba(255,255,255,0.03)' : 'rgba(248, 250, 252, 0.9)',
    tdColor: 'transparent',
    thColorHover: shellStore.isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(245, 247, 250, 0.96)',
    tdColorHover: shellStore.isDarkMode ? 'rgba(255,255,255,0.02)' : 'rgba(79, 124, 255, 0.02)',
  },
  Menu: {
    itemBorderRadius: '14px',
    itemHeight: '44px',
  },
}));
</script>
