<template>
  <div class="state-page">
    <t-card title="暂无访问权限" class="state-card">
      <p>当前账号没有访问该页面的权限，请联系超级管理员调整后台角色。</p>
      <t-space>
        <t-button theme="primary" @click="goHome">回到首页</t-button>
        <t-button variant="outline" @click="goLogin">重新登录</t-button>
      </t-space>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAdminAuthStore } from '../../stores/auth';
import { useAdminPermissionStore } from '../../stores/permission';

const router = useRouter();
const authStore = useAdminAuthStore();
const permissionStore = useAdminPermissionStore();

async function goHome() {
  await router.replace(permissionStore.homePath || '/');
}

async function goLogin() {
  permissionStore.reset(router);
  authStore.clearSession();
  await router.replace('/login');
}
</script>

<style scoped>
.state-page {
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
}

.state-card {
  width: min(520px, 100%);
}
</style>
