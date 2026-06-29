<template>
  <div class="page">
    <t-card title="Life 管理端登录" class="card">
      <t-form @submit="onSubmit">
        <t-form-item label="用户名" name="username">
          <t-input v-model="form.username" placeholder="请输入管理员用户名" />
        </t-form-item>
        <t-form-item label="密码" name="password">
          <t-input v-model="form.password" type="password" placeholder="请输入密码" />
        </t-form-item>
        <t-button theme="primary" type="submit" block>登录</t-button>
      </t-form>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import { http } from '../services/http';
import { useAdminAuthStore } from '../stores/auth';

const router = useRouter();
const authStore = useAdminAuthStore();
const form = reactive({
  username: 'admin',
  password: 'admin123456',
});

async function onSubmit() {
  try {
    const response = await http.post('/api/auth/login', form);
    authStore.setAccessToken(response.data.data.accessToken);
    await router.push('/');
  } catch (error) {
    MessagePlugin.error('登录失败，请确认管理员账号已初始化');
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #f4f8ff 0%, #eef6f1 100%);
}

.card {
  width: min(420px, calc(100vw - 32px));
}
</style>
