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

      <van-form class="card">
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
          <van-button block type="primary" native-type="button" :loading="submitting" @click="openCaptchaPopup">
            登录
          </van-button>
          <van-button block plain type="default" native-type="button" :loading="submitting" @click="register">
            注册账号
          </van-button>
        </div>
        <p class="agreement">
          登录即表示同意《用户协议》与《隐私政策》
        </p>
      </van-form>
    </section>

    <van-popup
      v-model:show="captchaVisible"
      round
      position="bottom"
      class="captcha-popup"
      :close-on-click-overlay="!submitting"
      @closed="handleCaptchaClosed"
    >
      <section class="captcha-popup__panel">
        <div class="captcha-popup__head">
          <strong>安全验证</strong>
          <button class="captcha-link" type="button" :disabled="submitting" @click="closeCaptchaPopup">
            关闭
          </button>
        </div>
        <p class="captcha-popup__tip">
          <template v-if="captcha">
            请依次点击图中的
            <span v-for="target in captcha.targets" :key="target" class="captcha-chip">{{ target }}</span>
          </template>
          <template v-else>正在生成点选验证码...</template>
        </p>
        <div
          class="captcha-board"
          :class="{ 'is-loading': loadingCaptcha }"
          @click="handleCaptchaBoardClick"
        >
          <img
            v-if="captcha"
            class="captcha-image"
            :src="captcha.imageData"
            alt="登录点选验证码"
          />
          <div v-else class="captcha-placeholder">请稍候，正在准备验证图</div>
          <span
            v-for="(point, index) in captchaPoints"
            :key="`${point.x}-${point.y}-${index}`"
            class="captcha-marker"
            :style="markerStyle(point)"
          >
            {{ index + 1 }}
          </span>
        </div>
        <div class="captcha-popup__status">
          <span>{{ captchaStatusText }}</span>
          <div class="captcha-popup__links">
            <button class="captcha-link" type="button" :disabled="loadingCaptcha || submitting" @click="reloadCaptcha">
              换一张
            </button>
            <button class="captcha-link" type="button" :disabled="captchaPoints.length === 0 || submitting" @click="resetCaptchaSelection">
              重选
            </button>
          </div>
        </div>
        <div class="captcha-popup__actions">
          <van-button plain block native-type="button" :disabled="submitting" @click="closeCaptchaPopup">
            取消
          </van-button>
          <van-button block type="primary" native-type="button" :loading="submitting" @click="confirmCaptchaAndLogin">
            验证并登录
          </van-button>
        </div>
      </section>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showToast } from 'vant';
import { useUserAuthStore } from '../stores/auth';
import { authApi, type CaptchaClickPoint, type LoginCaptchaChallenge } from '../services/api';

const heroLogo = new URL('../assets/login-hero-logo.png', import.meta.url).href;

const route = useRoute();
const router = useRouter();
const authStore = useUserAuthStore();
const submitting = ref(false);
const loadingCaptcha = ref(false);
const captchaVisible = ref(false);
const captcha = ref<LoginCaptchaChallenge | null>(null);
const captchaPoints = ref<CaptchaClickPoint[]>([]);
const form = reactive({
  username: '',
  password: '',
});

const captchaStatusText = computed(() => {
  if (!captcha.value) {
    return '验证码加载中';
  }
  return `已选择 ${captchaPoints.value.length}/${captcha.value.targets.length}`;
});

function resolveRedirectPath() {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
  return redirect.startsWith('/') ? redirect : '/';
}

function ensureCredentialReady() {
  if (!form.username.trim()) {
    showToast('请输入用户名');
    return false;
  }
  if (!form.password.trim()) {
    showToast('请输入密码');
    return false;
  }
  return true;
}

async function loadCaptcha() {
  loadingCaptcha.value = true;
  try {
    captcha.value = await authApi.fetchLoginCaptcha();
    captchaPoints.value = [];
  } finally {
    loadingCaptcha.value = false;
  }
}

async function openCaptchaPopup() {
  if (submitting.value || !ensureCredentialReady()) {
    return;
  }
  captchaVisible.value = true;
  await loadCaptcha();
}

async function reloadCaptcha() {
  await loadCaptcha();
}

function closeCaptchaPopup() {
  if (submitting.value) {
    return;
  }
  captchaVisible.value = false;
}

function handleCaptchaClosed() {
  captcha.value = null;
  captchaPoints.value = [];
}

function resetCaptchaSelection() {
  captchaPoints.value = [];
}

function markerStyle(point: CaptchaClickPoint) {
  if (!captcha.value) {
    return {};
  }
  return {
    left: `${(point.x / captcha.value.width) * 100}%`,
    top: `${(point.y / captcha.value.height) * 100}%`,
  };
}

function handleCaptchaBoardClick(event: MouseEvent) {
  if (!captcha.value || loadingCaptcha.value || submitting.value) {
    return;
  }
  if (captchaPoints.value.length >= captcha.value.targets.length) {
    showToast('已完成点选，可直接提交');
    return;
  }
  const board = event.currentTarget as HTMLElement | null;
  if (!board) {
    return;
  }
  const rect = board.getBoundingClientRect();
  const relativeX = (event.clientX - rect.left) / rect.width;
  const relativeY = (event.clientY - rect.top) / rect.height;
  if (relativeX < 0 || relativeX > 1 || relativeY < 0 || relativeY > 1) {
    return;
  }
  captchaPoints.value = [
    ...captchaPoints.value,
    {
      x: Math.round(relativeX * captcha.value.width),
      y: Math.round(relativeY * captcha.value.height),
    },
  ];
}

async function confirmCaptchaAndLogin() {
  if (!ensureCredentialReady()) {
    return;
  }
  if (!captcha.value) {
    showToast('验证码加载中，请稍后重试');
    await loadCaptcha();
    return;
  }
  if (captchaPoints.value.length !== captcha.value.targets.length) {
    showToast(`请按顺序点选 ${captcha.value.targets.length} 个字符`);
    return;
  }

  submitting.value = true;
  try {
    const verifyResult = await authApi.verifyLoginCaptcha({
      captchaId: captcha.value.captchaId,
      captchaPoints: captchaPoints.value,
    });
    await authStore.login(form.username.trim(), form.password, verifyResult.tempKey);
    captchaVisible.value = false;
    showSuccessToast('登录成功');
    await router.push(resolveRedirectPath());
  } catch {
    await loadCaptcha();
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

.captcha-popup {
  overflow: hidden;
}

.captcha-popup__panel {
  padding: 20px 18px calc(18px + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, #fffaf6 0%, #fff4ea 100%);
}

.captcha-popup__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.captcha-popup__head strong {
  font-size: 18px;
  color: var(--lf-color-text-primary);
}

.captcha-popup__tip {
  margin: 12px 0 0;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.captcha-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  margin-left: 6px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(255, 116, 72, 0.14);
  color: #d9481e;
  font-weight: 700;
}

.captcha-board {
  position: relative;
  margin-top: 14px;
  border-radius: 20px;
  overflow: hidden;
  aspect-ratio: 16 / 9;
  background: #fff;
  box-shadow: inset 0 0 0 1px rgba(255, 125, 87, 0.18);
  cursor: crosshair;
}

.captcha-board.is-loading {
  cursor: progress;
}

.captcha-image,
.captcha-placeholder {
  display: block;
  width: 100%;
  height: 100%;
}

.captcha-image {
  object-fit: cover;
}

.captcha-placeholder {
  display: grid;
  place-items: center;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
  background: linear-gradient(135deg, #fff8f3 0%, #fff3e8 100%);
}

.captcha-marker {
  position: absolute;
  transform: translate(-50%, -50%);
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #ff6c43;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 10px 18px rgba(255, 108, 67, 0.28);
  pointer-events: none;
}

.captcha-popup__status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.captcha-popup__links {
  display: inline-flex;
  gap: 12px;
}

.captcha-popup__actions {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.captcha-popup__actions :deep(.van-button--primary) {
  border: none;
  border-radius: 999px;
  height: 46px;
  background: linear-gradient(135deg, #ff7648 0%, #ff4a26 100%);
}

.captcha-popup__actions :deep(.van-button--default) {
  border-radius: 999px;
  height: 44px;
}

.captcha-link {
  padding: 0;
  border: none;
  background: transparent;
  color: #d9481e;
  font-size: 13px;
  font-weight: 600;
}

.captcha-link:disabled {
  color: #c5ced8;
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
