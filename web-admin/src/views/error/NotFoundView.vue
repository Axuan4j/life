<template>
  <div class="state-page">
    <t-card title="页面不存在" class="state-card">
      <p>当前地址没有对应的后台页面，可能是菜单权限已变更，或访问路径不正确。</p>
      <t-button theme="primary" @click="goHome">返回可访问首页</t-button>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAdminPermissionStore } from '../../stores/permission';

const router = useRouter();
const permissionStore = useAdminPermissionStore();

async function goHome() {
  await router.replace(permissionStore.homePath || '/');
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
