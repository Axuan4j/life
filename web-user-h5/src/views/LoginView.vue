<template>
  <div class="page">
    <section class="shell">
      <header class="brand">
        <span class="eyebrow">Quick Community</span>
        <div class="brand-logo">
          <strong>Life Feed</strong>
        </div>
        <p>记录生活，连接世界</p>
      </header>

      <section class="illustration">
        <img class="hero-logo" :src="heroLogo" alt="Life Feed Logo" />
      </section>

      <van-form class="card" @submit="onSubmit">
        <div class="form-head">
          <strong>密码登录</strong>
          <span>安全登录，开启内容之旅</span>
        </div>
        <van-field v-model="form.username" name="username" label="" placeholder="请输入用户名" />
        <van-field v-model="form.password" name="password" type="password" label="" placeholder="请输入密码" />
        <div class="hint-row">
          <span>账号密码均支持安全校验</span>
          <span>忘记密码</span>
        </div>
        <div class="actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">登录</van-button>
          <van-button block plain type="default" :loading="submitting" @click="register">注册账号</van-button>
        </div>
        <p class="agreement">
          登录即表示同意《用户协议》与《隐私政策》
        </p>
      </van-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { useUserAuthStore } from '../stores/auth';

const heroLogo = new URL('../assets/login-hero-logo.png', import.meta.url).href;

const route = useRoute();
const router = useRouter();
const authStore = useUserAuthStore();
const submitting = ref(false);
const form = reactive({
  username: 'life_user',
  password: 'life123456',
});

function resolveRedirectPath() {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
  return redirect.startsWith('/') ? redirect : '/';
}

async function onSubmit() {
  submitting.value = true;
  try {
    await authStore.login(form.username.trim(), form.password);
    showSuccessToast('登录成功');
    await router.push(resolveRedirectPath());
  } finally {
    submitting.value = false;
  }
}

async function register() {
  submitting.value = true;
  try {
    await authStore.register(form.username.trim(), form.password, form.username.trim());
    showSuccessToast('注册成功');
    await router.push(resolveRedirectPath());
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0;
  background: linear-gradient(180deg, #fff6f0 0%, #fff7ef 42%, #fffdfa 100%);
  overflow: hidden;
}

.shell {
  box-sizing: border-box;
  min-height: 100vh;
  width: min(100%, 100vw);
  max-width: 430px;
  margin: 0 auto;
  padding: 5.6vh 5.2vw 4vh;
}

.brand {
  padding-top: 10px;
  text-align: center;
}

.eyebrow {
  display: inline-block;
  margin-bottom: 1.2vh;
  color: var(--lf-color-primary);
  font-size: clamp(12px, 3vw, 13px);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  color: var(--lf-color-primary);
  font-size: clamp(20px, 6vw, 28px);
  line-height: 1;
}

.brand p {
  margin: 1.5vh 0 0;
  color: var(--lf-color-text-secondary);
  font-size: clamp(13px, 3.4vw, 14px);
}

.illustration {
  display: grid;
  place-items: center;
  min-height: 26vh;
  margin-top: 2vh;
}

.hero-logo {
  width: min(84vw, 100%);
  max-width: 310px;
  object-fit: contain;
  filter: drop-shadow(0 12px 20px rgba(26, 36, 56, 0.06));
}

.card {
  margin-top: 0.6vh;
  padding: 2.5vh 4.4vw 2.2vh;
  border-radius: min(26px, 6vw);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 36px rgba(28, 38, 60, 0.08);
}

.form-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 1.6vh;
}

.form-head strong {
  font-size: clamp(20px, 5.6vw, 22px);
  color: var(--lf-color-text-primary);
}

.form-head span {
  color: var(--lf-color-text-secondary);
  font-size: clamp(12px, 3vw, 13px);
}

.card :deep(.van-cell) {
  margin-bottom: 1.2vh;
  padding: 1.6vh 4vw;
  border-radius: min(18px, 4.8vw);
  background: #f7f8fc;
  box-shadow: inset 0 0 0 1px rgba(229, 231, 235, 0.55);
}

.card :deep(.van-field__control) {
  font-size: clamp(15px, 3.8vw, 16px);
}

.hint-row {
  display: flex;
  justify-content: space-between;
  margin-top: 2px;
  color: var(--lf-color-text-secondary);
  font-size: clamp(12px, 3vw, 13px);
}

.actions {
  display: grid;
  gap: 12px;
  margin-top: 2.2vh;
}

.actions :deep(.van-button--primary) {
  border: none;
  border-radius: 999px;
  height: clamp(46px, 6.2vh, 52px);
  background: linear-gradient(135deg, #ff7648 0%, #ff4a26 100%);
}

.actions :deep(.van-button--default) {
  border-radius: 999px;
  height: clamp(44px, 5.8vh, 50px);
}

.agreement {
  margin: 2vh 0 0;
  text-align: center;
  color: #98a2b3;
  font-size: clamp(12px, 3vw, 13px);
}

@media (max-height: 760px) {
  .shell {
    padding-top: 3.6vh;
    padding-bottom: 2.8vh;
  }

  .illustration {
    min-height: 22vh;
  }

  .actions {
    margin-top: 1.6vh;
  }
}
</style>
