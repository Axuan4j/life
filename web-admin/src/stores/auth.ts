import { computed, ref } from 'vue';
import { defineStore } from 'pinia';

const ADMIN_ACCESS_TOKEN_KEY = 'life_admin_access_token';

export const useAdminAuthStore = defineStore('admin-auth', () => {
  const accessToken = ref(localStorage.getItem(ADMIN_ACCESS_TOKEN_KEY) ?? '');

  const isAuthenticated = computed(() => Boolean(accessToken.value));

  function setAccessToken(token: string) {
    accessToken.value = token;
    localStorage.setItem(ADMIN_ACCESS_TOKEN_KEY, token);
  }

  function clearAccessToken() {
    accessToken.value = '';
    localStorage.removeItem(ADMIN_ACCESS_TOKEN_KEY);
  }

  return {
    accessToken,
    isAuthenticated,
    setAccessToken,
    clearAccessToken,
  };
});
