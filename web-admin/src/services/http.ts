import axios from 'axios';
import { useAdminAuthStore } from '../stores/auth';

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  timeout: 10000,
});

http.interceptors.request.use((config) => {
  const authStore = useAdminAuthStore();
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`;
  }
  return config;
});
