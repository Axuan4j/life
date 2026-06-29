import { createRouter, createWebHistory } from 'vue-router';
import { useUserAuthStore } from '../stores/auth';
import DiscoverView from '../views/DiscoverView.vue';
import DiscoverResultView from '../views/DiscoverResultView.vue';
import FeedHomeView from '../views/FeedHomeView.vue';
import LoginView from '../views/LoginView.vue';
import MessageView from '../views/MessageView.vue';
import PostDetailView from '../views/PostDetailView.vue';
import PostComposerView from '../views/PostComposerView.vue';
import ProfileView from '../views/ProfileView.vue';
import UserProfileView from '../views/UserProfileView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView },
    { path: '/', name: 'home', component: FeedHomeView, meta: { requiresAuth: true, showTabbar: true } },
    { path: '/posts/:postId', name: 'post-detail', component: PostDetailView, meta: { requiresAuth: true, showTabbar: false } },
    { path: '/discover', name: 'discover', component: DiscoverView, meta: { requiresAuth: true, showTabbar: true } },
    { path: '/discover/result', name: 'discover-result', component: DiscoverResultView, meta: { requiresAuth: true, showTabbar: false } },
    { path: '/compose', name: 'compose', component: PostComposerView, meta: { requiresAuth: true, showTabbar: true } },
    { path: '/message', name: 'message', component: MessageView, meta: { requiresAuth: true, showTabbar: true } },
    { path: '/profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true, showTabbar: true } },
    { path: '/users/:userId', name: 'user-profile', component: UserProfileView, meta: { requiresAuth: true, showTabbar: false } },
  ],
});

router.beforeEach((to) => {
  const authStore = useUserAuthStore();
  // H5 端统一在路由层拦住未登录访问，避免页面内部各自判断导致首屏闪烁。
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login' };
  }
  if (to.name === 'login' && authStore.isAuthenticated) {
    return { name: 'home' };
  }
  return true;
});

export default router;
