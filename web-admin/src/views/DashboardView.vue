<template>
  <div class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Life Admin</p>
        <h1>内容平台后台骨架已就绪</h1>
        <p class="description">下一步可以在这里扩展用户管理、内容审核、推荐位配置和运营统计。</p>
      </div>
      <t-button variant="outline" @click="logout">退出登录</t-button>
    </header>

    <section class="grid">
      <t-card title="用户管理" bordered>预留用户列表、封禁、角色管理入口。</t-card>
      <t-card title="内容审核" bordered>预留帖子审核、媒体审核与举报处理入口。</t-card>
      <t-card title="推荐运营" bordered>预留推荐位配置与首页运营策略入口。</t-card>
      <t-card title="基础统计" bordered>预留日活、发帖、互动等基础指标看板。</t-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAdminAuthStore } from '../stores/auth';

const router = useRouter();
const authStore = useAdminAuthStore();

async function logout() {
  authStore.clearAccessToken();
  await router.push('/login');
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(0, 112, 243, 0.12), transparent 28%),
    radial-gradient(circle at top right, rgba(39, 174, 96, 0.1), transparent 24%),
    #f7f9fc;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
  align-items: flex-start;
}

.eyebrow {
  margin: 0 0 8px;
  color: #0052d9;
  font-weight: 600;
}

h1 {
  margin: 0 0 8px;
}

.description {
  margin: 0;
  color: #5f6b7a;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}
</style>
