import axios from 'axios';
import { showToast } from 'vant';
import { useUserAuthStore } from '../stores/auth';

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  timeout: 10000,
});

let isHandlingUnauthorized = false;

http.interceptors.request.use((config) => {
  const authStore = useUserAuthStore();
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    // 登录态失效必须在一个地方统一收口，否则不同页面会出现提示不一致、重复弹窗和不跳登录页的问题。
    if (error.response?.status === 401) {
      const authStore = useUserAuthStore();
      authStore.clearTokens();

      if (!isHandlingUnauthorized) {
        isHandlingUnauthorized = true;
        showToast('登录已失效，请重新登录');
        if (window.location.pathname !== '/login') {
          const redirect = `${window.location.pathname}${window.location.search}`;
          window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`;
          return Promise.reject(error);
        }
        window.setTimeout(() => {
          isHandlingUnauthorized = false;
        }, 300);
      }
      return Promise.reject(error);
    }
    showToast(error.response?.data?.message ?? '请求失败，请稍后再试');
    return Promise.reject(error);
  },
);
