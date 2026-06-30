<template>
  <router-view />
</template>

<script setup lang="ts">
import { useAdminShellStore } from './stores/shell';

const shellStore = useAdminShellStore();

shellStore.initializeThemeMode();
</script>

<style>
:root {
  --life-admin-accent: #e86d4f;
  --life-admin-accent-soft: rgba(232, 109, 79, 0.12);
  --life-admin-border: rgba(25, 35, 52, 0.08);
  --life-admin-shadow: 0 18px 42px rgba(21, 35, 58, 0.08);
}

html,
body,
#app {
  min-height: 100%;
}

body {
  margin: 0;
  background:
    radial-gradient(circle at top left, rgba(255, 199, 183, 0.68) 0%, transparent 30%),
    radial-gradient(circle at top right, rgba(198, 225, 255, 0.6) 0%, transparent 28%),
    linear-gradient(180deg, #fffaf7 0%, #f5f7fb 52%, #eef3f8 100%);
  color: var(--td-text-color-primary);
}

html[theme-mode='dark'] body {
  background:
    radial-gradient(circle at top left, rgba(232, 109, 79, 0.18) 0%, transparent 28%),
    radial-gradient(circle at top right, rgba(89, 159, 255, 0.18) 0%, transparent 26%),
    linear-gradient(180deg, #131821 0%, #10151d 58%, #0d1219 100%);
}

.t-card {
  border: 1px solid var(--life-admin-border);
  border-radius: 24px;
  box-shadow: var(--life-admin-shadow);
  overflow: hidden;
}

.t-card__header {
  padding: 20px 22px 12px;
}

.t-card__body {
  padding: 20px 22px 22px;
}

.t-table {
  border-radius: 18px;
  overflow: hidden;
}

.t-table thead tr {
  background: rgba(242, 246, 251, 0.92);
}

html[theme-mode='dark'] .t-table thead tr {
  background: rgba(255, 255, 255, 0.04);
}

.t-input,
.t-select,
.t-textarea,
.t-pagination {
  border-radius: 14px;
}

.t-button.t-button--theme-primary {
  border-color: transparent;
  background: linear-gradient(135deg, #ed7b59 0%, #d85b3c 100%);
  box-shadow: 0 10px 24px rgba(216, 91, 60, 0.2);
}

.t-button.t-button--theme-primary:not(.t-is-disabled):hover {
  background: linear-gradient(135deg, #f08a6b 0%, #db6345 100%);
}
</style>
