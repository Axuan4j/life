<template>
  <div class="login-page">
    <div class="login-page__shell">
      <section class="login-hero">
        <span class="login-hero__eyebrow">Life 后台</span>
        <h1>欢迎登录管理后台</h1>
        <p>在这里处理用户、帖子、权限和站内广播等日常工作。</p>

        <div class="login-hero__metrics">
          <article>
            <strong>用户</strong>
            <span>查看账号和基础数据</span>
          </article>
          <article>
            <strong>内容</strong>
            <span>处理帖子和广播消息</span>
          </article>
          <article>
            <strong>权限</strong>
            <span>角色与菜单配置</span>
          </article>
        </div>

        <div class="login-hero__panel">
          <div class="login-hero__panel-head">
            <span class="login-hero__dot"></span>
            <span>常用功能</span>
          </div>
          <ul>
            <li>查看用户、帖子和首页概览数据</li>
            <li>配置角色、菜单和管理员权限</li>
            <li>发送站内广播消息</li>
          </ul>
        </div>
      </section>

      <section class="login-form-shell">
        <n-card class="login-card" :bordered="false">
          <div class="login-card__head">
            <div class="login-card__logo">L</div>
            <div>
              <strong>后台登录</strong>
              <p>请输入管理员账号和密码</p>
            </div>
          </div>

          <n-form class="login-form" label-placement="top" @submit.prevent="handleSubmit">
            <n-form-item label="用户名">
              <n-input v-model:value="form.username" size="large" placeholder="请输入管理员用户名">
                <template #prefix>
                  <n-icon :component="PersonOutline" />
                </template>
              </n-input>
            </n-form-item>

            <n-form-item label="密码">
              <n-input
                v-model:value="form.password"
                size="large"
                type="password"
                show-password-on="mousedown"
                placeholder="请输入登录密码"
                @keydown.enter.prevent="handleSubmit"
              >
                <template #prefix>
                  <n-icon :component="LockClosedOutline" />
                </template>
              </n-input>
            </n-form-item>

            <div class="login-form__meta">
              <n-checkbox v-model:checked="rememberMe">保留已输入账号</n-checkbox>
              <span>联调账号：admin</span>
            </div>

            <n-button type="primary" size="large" block :loading="submitting" @click="handleSubmit">
              登录后台
            </n-button>
          </n-form>
        </n-card>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LockClosedOutline, PersonOutline } from '@vicons/ionicons5';
import { NButton, NCard, NCheckbox, NForm, NFormItem, NIcon, NInput } from 'naive-ui';
import { login } from '../../services/api';
import { useAdminAuthStore } from '../../stores/auth';
import { useAdminPermissionStore } from '../../stores/permission';
import { message } from '../../utils/feedback';

const router = useRouter();
const route = useRoute();
const authStore = useAdminAuthStore();
const permissionStore = useAdminPermissionStore();
const submitting = ref(false);
const rememberMe = ref(true);
const form = reactive({
  username: '',
  password: '',
});

async function handleSubmit() {
  if (!form.username.trim() || !form.password.trim()) {
    message.warning('请输入用户名和密码');
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
    if (!rememberMe.value) {
      form.password = '';
    }
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
  padding: 28px;
}

.login-page__shell {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(380px, 460px);
  gap: 28px;
  align-items: stretch;
  min-height: calc(100vh - 56px);
}

.login-hero,
.login-form-shell {
  min-height: 100%;
}

.login-hero {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 44px 48px;
  border: 1px solid var(--life-border);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--life-shadow);
}

.login-hero__eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--life-primary-soft);
  color: var(--life-primary);
  font-size: 12px;
  font-weight: 700;
}

.login-hero h1 {
  margin: 24px 0 0;
  max-width: 700px;
  color: var(--life-text);
  font-size: 38px;
  line-height: 1.16;
}

.login-hero p {
  margin: 18px 0 0;
  max-width: 680px;
  color: var(--life-text-muted);
  font-size: 15px;
  line-height: 1.8;
}

.login-hero__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 28px;
}

.login-hero__metrics article,
.login-hero__panel {
  border: 1px solid var(--life-border);
  border-radius: 20px;
  background: var(--life-bg-soft);
}

.login-hero__metrics article {
  padding: 18px;
}

.login-hero__metrics strong {
  display: block;
  color: var(--life-text);
  font-size: 18px;
}

.login-hero__metrics span {
  display: block;
  margin-top: 10px;
  color: var(--life-text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.login-hero__panel {
  margin-top: 20px;
  padding: 20px 22px;
}

.login-hero__panel-head {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--life-text);
  font-size: 14px;
  font-weight: 600;
}

.login-hero__dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, #4f7cff 0%, #79a9ff 100%);
}

.login-hero__panel ul {
  margin: 16px 0 0;
  padding-left: 18px;
  color: var(--life-text-muted);
  font-size: 14px;
  line-height: 1.9;
}

.login-form-shell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 440px;
  border-radius: 28px;
  background: var(--life-card-strong);
  box-shadow: var(--life-shadow);
}

.login-card__head {
  display: flex;
  align-items: center;
  gap: 16px;
}

.login-card__logo {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: linear-gradient(135deg, #4f7cff 0%, #79a9ff 100%);
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.login-card__head strong {
  display: block;
  color: var(--life-text);
  font-size: 24px;
}

.login-card__head p {
  margin: 8px 0 0;
  color: var(--life-text-muted);
  font-size: 13px;
}

.login-form {
  margin-top: 26px;
}

.login-form__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: -2px 0 20px;
  color: var(--life-text-soft);
  font-size: 12px;
}

@media (max-width: 1100px) {
  .login-page__shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .login-hero {
    padding: 32px 28px;
  }

  .login-hero h1 {
    font-size: 34px;
  }

  .login-hero__metrics {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 16px;
  }

  .login-hero h1 {
    font-size: 28px;
  }

  .login-form__meta {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
