<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-copy">
        <span class="copy-badge">Life Admin</span>
        <h1>把内容平台的运营节奏、权限边界和数据视图放到一个清晰的后台里。</h1>
        <p>
          第一版后台以稳定联调和高频管理任务为核心，覆盖用户、帖子、权限和广播消息。
        </p>
        <div class="copy-metrics">
          <article>
            <strong>RBAC</strong>
            <span>动态菜单权限</span>
          </article>
          <article>
            <strong>Feed</strong>
            <span>内容与互动联调</span>
          </article>
          <article>
            <strong>SSE</strong>
            <span>广播与消息提醒</span>
          </article>
        </div>
      </section>

      <t-card class="login-card">
        <template #title>
          <div class="card-title">
            <strong>登录后台</strong>
            <span>管理员账号验证后进入工作台</span>
          </div>
        </template>
        <t-form label-align="top" class="login-form" @submit.prevent="handleSubmit">
          <t-form-item label="用户名">
            <t-input v-model="form.username" size="large" placeholder="请输入管理员用户名" />
          </t-form-item>
          <t-form-item label="密码">
            <t-input v-model="form.password" size="large" type="password" placeholder="请输入密码" />
          </t-form-item>
          <t-button theme="primary" size="large" block :loading="submitting" @click="handleSubmit">登录后台</t-button>
        </t-form>
      </t-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import { login } from '../../services/api';
import { useAdminAuthStore } from '../../stores/auth';
import { useAdminPermissionStore } from '../../stores/permission';

const router = useRouter();
const route = useRoute();
const authStore = useAdminAuthStore();
const permissionStore = useAdminPermissionStore();
const submitting = ref(false);
const form = reactive({
  username: 'admin',
  password: 'admin123456',
});

async function handleSubmit() {
  if (!form.username.trim() || !form.password.trim()) {
    MessagePlugin.warning('请输入用户名和密码');
    return;
  }

  submitting.value = true;
  try {
    const response = await login({
      username: form.username.trim(),
      password: form.password.trim(),
    });
    authStore.setTokens(response.accessToken, response.refreshToken);
    authStore.setOperator(response.operator);
    permissionStore.applySession(router, response);
    const redirectPath =
      typeof route.query.redirect === 'string' && route.query.redirect
        ? route.query.redirect
        : response.homePath || '/403';
    await router.replace(redirectPath);
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(255, 207, 189, 0.92) 0%, transparent 32%),
    radial-gradient(circle at bottom right, rgba(190, 223, 255, 0.72) 0%, transparent 28%),
    linear-gradient(180deg, #fffaf7 0%, #f4f7fb 100%);
  padding: 32px;
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(320px, 1.2fr) minmax(360px, 440px);
  gap: 32px;
  align-items: stretch;
  max-width: 1180px;
  min-height: calc(100vh - 64px);
  margin: 0 auto;
}

.login-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 44px 40px;
  border: 1px solid rgba(25, 35, 52, 0.06);
  border-radius: 36px;
  background:
    radial-gradient(circle at top right, rgba(255, 219, 206, 0.85) 0%, transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.88) 0%, rgba(252, 245, 241, 0.92) 100%);
  box-shadow: 0 26px 52px rgba(21, 35, 58, 0.08);
}

.copy-badge {
  display: inline-flex;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(232, 109, 79, 0.12);
  color: #d35c3e;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.login-copy h1 {
  margin: 20px 0 0;
  color: #1d2735;
  font-size: 38px;
  line-height: 1.18;
}

.login-copy p {
  margin: 16px 0 0;
  max-width: 520px;
  color: #5f6b7a;
  font-size: 15px;
  line-height: 1.8;
}

.copy-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 28px;
}

.copy-metrics article {
  padding: 16px 18px;
  border: 1px solid rgba(25, 35, 52, 0.06);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.68);
}

.copy-metrics strong {
  display: block;
  color: #1f2b39;
  font-size: 18px;
}

.copy-metrics span {
  display: block;
  margin-top: 8px;
  color: #6c7785;
  font-size: 13px;
}

.login-card {
  width: 100%;
  align-self: center;
}

.card-title {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-title strong {
  color: var(--td-text-color-primary);
  font-size: 24px;
}

.card-title span {
  color: var(--td-text-color-secondary);
  font-size: 13px;
}

.login-form {
  margin-top: 6px;
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .login-copy {
    padding: 28px 24px;
  }

  .login-copy h1 {
    font-size: 30px;
  }

  .copy-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
