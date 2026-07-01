<template>
  <div class="admin-state-page">
    <n-card class="admin-card admin-state-card" :bordered="false">
      <n-result
        status="404"
        title="页面不存在"
        description="当前地址没有对应的后台页面，可能是权限菜单已变更，或者访问路径不正确。"
      >
        <template #footer>
          <n-button type="primary" @click="goHome">返回首页</n-button>
        </template>
      </n-result>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { NButton, NCard, NResult } from 'naive-ui';
import { useRouter } from 'vue-router';
import { useAdminPermissionStore } from '../../stores/permission';

const router = useRouter();
const permissionStore = useAdminPermissionStore();

async function goHome() {
  await router.replace(permissionStore.homePath || '/');
}
</script>
