import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import type { AdminOperator } from '../types/admin';

const ADMIN_ACCESS_TOKEN_KEY = 'life_admin_access_token';
const ADMIN_REFRESH_TOKEN_KEY = 'life_admin_refresh_token';

export const useAdminAuthStore = defineStore('admin-auth', () => {
  const accessToken = ref(localStorage.getItem(ADMIN_ACCESS_TOKEN_KEY) ?? '');
  const refreshToken = ref(localStorage.getItem(ADMIN_REFRESH_TOKEN_KEY) ?? '');
  const operator = ref<AdminOperator | null>(null);

  const isAuthenticated = computed(() => Boolean(accessToken.value));

  function setTokens(nextAccessToken: string, nextRefreshToken: string) {
    accessToken.value = nextAccessToken;
    refreshToken.value = nextRefreshToken;
    localStorage.setItem(ADMIN_ACCESS_TOKEN_KEY, nextAccessToken);
    localStorage.setItem(ADMIN_REFRESH_TOKEN_KEY, nextRefreshToken);
  }

  function setOperator(nextOperator: AdminOperator | null) {
    operator.value = nextOperator;
  }

  function clearSession() {
    accessToken.value = '';
    refreshToken.value = '';
    operator.value = null;
    localStorage.removeItem(ADMIN_ACCESS_TOKEN_KEY);
    localStorage.removeItem(ADMIN_REFRESH_TOKEN_KEY);
  }

  return {
    accessToken,
    refreshToken,
    operator,
    isAuthenticated,
    setTokens,
    setOperator,
    clearSession,
  };
});
