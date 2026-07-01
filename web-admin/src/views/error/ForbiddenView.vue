<template>
  <div class="admin-state-page">
    <n-card class="admin-card admin-state-card" :bordered="false">
      <n-result
        status="403"
        title="暂无访问权限"
        description="当前账号没有访问这个后台页面的权限，请联系超级管理员调整角色。"
      >
        <template #footer>
          <div class="admin-action-group">
            <n-button type="primary" @click="goHome">回到首页</n-button>
            <n-button secondary @click="goLogin">重新登录</n-button>
          </div>
        </template>
      </n-result>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { NButton, NCard, NResult } from 'naive-ui';
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
