import axios, { type InternalAxiosRequestConfig } from 'axios';
import { MessagePlugin } from 'tdesign-vue-next';
import { useAdminAuthStore } from '../stores/auth';
import type { ApiResponse } from '../types/http';

interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
  _skipAuthRefresh?: boolean;
}

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const http = axios.create({
  baseURL,
  timeout: 15000,
});

const refreshHttp = axios.create({
  baseURL,
  timeout: 15000,
});

let refreshTokenPromise: Promise<string> | null = null;
let isHandlingUnauthorized = false;

function parseErrorMessage(error: unknown, fallback = '请求失败') {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message || error.message || fallback;
  }
  if (error instanceof Error) {
    return error.message || fallback;
  }
  return fallback;
}

function buildLoginRedirectPath() {
  const redirect = `${window.location.pathname}${window.location.search}${window.location.hash}`;
  return `/login?redirect=${encodeURIComponent(redirect)}`;
}

function shouldSkipRefresh(config?: RetryableRequestConfig) {
  const requestUrl = config?.url ?? '';
  return Boolean(
    !config ||
      config._skipAuthRefresh ||
      requestUrl.includes('/api/admin/auth/login') ||
      requestUrl.includes('/api/auth/refresh'),
  );
}

function handleUnauthorized(message: string) {
  const authStore = useAdminAuthStore();
  authStore.clearSession();

  if (!isHandlingUnauthorized) {
    isHandlingUnauthorized = true;
    MessagePlugin.error(message || '登录已失效，请重新登录');

    if (window.location.pathname !== '/login') {
      window.location.replace(buildLoginRedirectPath());
      return;
    }

    window.setTimeout(() => {
      isHandlingUnauthorized = false;
    }, 300);
  }
}

async function refreshAccessToken() {
  const authStore = useAdminAuthStore();
  if (!authStore.refreshToken) {
    throw new Error('登录已过期，请重新登录');
  }

  if (!refreshTokenPromise) {
    // 多个请求同时 401 时只发一次 refresh，请求恢复后继续重放原请求，避免刷新风暴。
    refreshTokenPromise = refreshHttp
      .post<ApiResponse<RefreshTokenResponse>>(
        '/api/auth/refresh',
        { refreshToken: authStore.refreshToken },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        },
      )
      .then((response) => {
        const envelope = response.data;
        if (!envelope || envelope.code !== 0 || !envelope.data?.accessToken || !envelope.data?.refreshToken) {
          throw new Error(envelope?.message || '登录已过期，请重新登录');
        }
        authStore.setTokens(envelope.data.accessToken, envelope.data.refreshToken);
        return envelope.data.accessToken;
      })
      .finally(() => {
        refreshTokenPromise = null;
      });
  }

  return refreshTokenPromise;
}

http.interceptors.request.use((config) => {
  const authStore = useAdminAuthStore();
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const envelope = response.data as ApiResponse<unknown> | undefined;
    if (envelope && typeof envelope.code === 'number' && envelope.code !== 0) {
      MessagePlugin.error(envelope.message || '请求失败');
      return Promise.reject(new Error(envelope.message || '请求失败'));
    }
    return response;
  },
  async (error) => {
    const authStore = useAdminAuthStore();
    const originalRequest = error.config as RetryableRequestConfig | undefined;
    const status = error.response?.status as number | undefined;
    const message = parseErrorMessage(error);

    if (status === 401) {
      if (originalRequest && !originalRequest._retry && !shouldSkipRefresh(originalRequest)) {
        originalRequest._retry = true;
        try {
          const nextAccessToken = await refreshAccessToken();
          originalRequest.headers = originalRequest.headers ?? {};
          originalRequest.headers.Authorization = `Bearer ${nextAccessToken}`;
          return http(originalRequest);
        } catch (refreshError) {
          handleUnauthorized(parseErrorMessage(refreshError, '登录已过期，请重新登录'));
          return Promise.reject(refreshError);
        }
      }

      handleUnauthorized(message || '登录已失效，请重新登录');
      return Promise.reject(error);
    }

    if (status === 403) {
      MessagePlugin.error(message);
      if (authStore.accessToken && window.location.pathname !== '/403') {
        window.location.replace('/403');
      }
      return Promise.reject(error);
    }

    MessagePlugin.error(message);
    return Promise.reject(error);
  },
);
