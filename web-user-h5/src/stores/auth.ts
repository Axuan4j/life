import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { authApi, userApi, type UserProfileResponse } from '../services/api';

const ACCESS_TOKEN_KEY = 'life_user_access_token';
const REFRESH_TOKEN_KEY = 'life_user_refresh_token';

export const useUserAuthStore = defineStore('user-auth', () => {
  const accessToken = ref(localStorage.getItem(ACCESS_TOKEN_KEY) ?? '');
  const refreshToken = ref(localStorage.getItem(REFRESH_TOKEN_KEY) ?? '');
  const currentUser = ref<UserProfileResponse | null>(null);

  const isAuthenticated = computed(() => Boolean(accessToken.value));

  function setTokens(nextAccessToken: string, nextRefreshToken: string) {
    accessToken.value = nextAccessToken;
    refreshToken.value = nextRefreshToken;
    localStorage.setItem(ACCESS_TOKEN_KEY, nextAccessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, nextRefreshToken);
  }

  function clearTokens() {
    accessToken.value = '';
    refreshToken.value = '';
    currentUser.value = null;
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }

  async function login(username: string, password: string) {
    const tokens = await authApi.login({ username, password });
    setTokens(tokens.accessToken, tokens.refreshToken);
    await fetchCurrentUser();
  }

  async function register(username: string, password: string, nickname?: string) {
    const tokens = await authApi.register({ username, password, nickname });
    setTokens(tokens.accessToken, tokens.refreshToken);
    await fetchCurrentUser();
  }

  async function fetchCurrentUser() {
    if (!accessToken.value) {
      currentUser.value = null;
      return null;
    }
    const me = await userApi.getMe();
    currentUser.value = me;
    return me;
  }

  async function logout() {
    try {
      if (accessToken.value) {
        await authApi.logout();
      }
    } finally {
      clearTokens();
    }
  }

  return {
    accessToken,
    refreshToken,
    currentUser,
    isAuthenticated,
    setTokens,
    clearTokens,
    login,
    register,
    fetchCurrentUser,
    logout,
  };
});
