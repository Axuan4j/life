<template>
  <div class="page">
    <section class="hero">
      <div class="hero-actions">
        <span>⌕</span>
        <span>⚙</span>
      </div>
      <div class="hero-main">
        <div
          class="avatar"
          :style="
            profile?.avatarUrl
              ? { backgroundImage: `url(${profile.avatarUrl})` }
              : { background: getFallbackAvatar(profile?.displayName ?? '我') }
          "
        ></div>
        <div class="hero-copy">
          <div class="hero-line">
            <h1>{{ profile?.displayName ?? '未登录用户' }}</h1>
            <span class="level">@{{ profile?.username ?? 'guest' }}</span>
          </div>
          <p>{{ profile?.bio }}</p>
        </div>
      </div>
      <div class="hero-stats">
        <article>
          <strong>{{ profile?.postCount ?? 0 }}</strong>
          <span>帖子</span>
        </article>
        <article>
          <strong>{{ profile?.followingCount ?? 0 }}</strong>
          <span>关注</span>
        </article>
        <article>
          <strong>{{ profile?.followerCount ?? 0 }}</strong>
          <span>粉丝</span>
        </article>
        <article>
          <strong>{{ profile?.likedCount ?? 0 }}</strong>
          <span>获赞</span>
        </article>
      </div>
      <div class="login-region">
        最近登录地：{{ profile?.lastLoginRegion || '未知' }}
      </div>
    </section>
    <section class="quick-actions">
      <div>个人资料</div>
      <div>我的关注</div>
      <div>互动记录</div>
      <div>账号设置</div>
    </section>
    <section v-if="checkInStatus" class="checkin-panel">
      <div class="checkin-head">
        <div>
          <strong>每日签到</strong>
          <p>本月已签到 {{ checkInStatus.signedCount }} 天</p>
        </div>
        <van-button
          round
          size="small"
          type="primary"
          class="checkin-button"
          :loading="signingIn"
          :disabled="checkInStatus.checkedInToday"
          @click="handleCheckIn"
        >
          {{ checkInStatus.checkedInToday ? '今日已签到' : '签到 +1 积分' }}
        </van-button>
      </div>
      <div class="checkin-summary">
        <article>
          <span>当前积分</span>
          <strong>{{ checkInStatus.totalPoints }}</strong>
        </article>
        <article>
          <span>签到月份</span>
          <strong>{{ checkInMonthLabel }}</strong>
        </article>
      </div>
      <div class="checkin-days">
        <div
          v-for="day in monthDays"
          :key="`checkin-day-${day}`"
          class="checkin-day"
          :class="{
            'checkin-day--signed': signedDaySet.has(day),
            'checkin-day--today': todayOfMonth === day,
          }"
        >
          {{ day }}
        </div>
      </div>
    </section>
    <section class="panel">
      <div class="circle-card">
        <div class="panel-head">
          <strong>我的圈子</strong>
          <span>{{ followingProfiles.length }} 位已关注</span>
        </div>
        <div class="circle-list">
          <div v-for="item in followingProfiles" :key="item.userId" class="circle-item">
            <div
              class="thumb"
              :style="
                item.avatarUrl
                  ? { backgroundImage: `url(${item.avatarUrl})` }
                  : { background: getFallbackAvatar(item.displayName) }
              "
            ></div>
            <strong>{{ item.displayName }}</strong>
            <span>@{{ item.username }}</span>
          </div>
          <div v-if="followingProfiles.length === 0" class="empty-following">还没有关注任何人</div>
        </div>
      </div>
      <div class="item">
        <strong>创作者中心</strong>
        <span>></span>
      </div>
      <div class="item">
        <strong>帮助与反馈</strong>
        <span>></span>
      </div>
      <div class="item">
        <strong>设置</strong>
        <span>></span>
      </div>
    </section>
    <section class="posts-panel">
      <div class="panel-head">
        <strong>我的帖子</strong>
        <span>{{ userPosts.length }} 条内容</span>
      </div>
      <article v-for="item in userPosts" :key="item.postId" class="post-card">
        <p v-if="item.topic" class="post-topic"># {{ item.topic }}</p>
        <p v-if="item.displayContentText" class="post-content">{{ item.displayContentText }}</p>
        <div v-if="item.poll" class="poll-card">
          <div class="poll-head">
            <span class="poll-badge">投票</span>
            <strong>{{ item.poll.question }}</strong>
          </div>
          <div class="poll-options">
            <div v-for="(option, index) in item.poll.options" :key="`${item.postId}-poll-${index}`" class="poll-option">
              <span>{{ index + 1 }}</span>
              <strong>{{ option }}</strong>
            </div>
          </div>
        </div>
        <div v-if="item.images.length > 0" class="post-images">
          <div
            v-for="image in item.images"
            :key="image.id"
            class="post-image"
            :style="image.url ? { backgroundImage: `url(${image.url})` } : { background: image.palette }"
          ></div>
        </div>
        <div class="post-meta">
          <span>{{ item.publishedAt }}</span>
          <span>赞 {{ item.likeCount }} · 评 {{ item.commentCount }} · 转 {{ item.repostCount }}</span>
        </div>
      </article>
      <div v-if="userPosts.length === 0" class="empty-posts">你还没有发布任何内容</div>
    </section>
    <div class="logout-wrap">
      <van-button block round type="danger" size="large" class="logout-button" @click="logout">
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { useUserAuthStore } from '../stores/auth';
import { followApi, postApi, userApi, type UserCheckInStatusResponse } from '../services/api';
import { getFallbackAvatar, mapPostCardToFeedPost, mapUserProfile } from '../services/view-models';

const router = useRouter();
const authStore = useUserAuthStore();
const followingProfiles = ref<ReturnType<typeof mapUserProfile>[]>([]);
const userPosts = ref<ReturnType<typeof mapPostCardToFeedPost>[]>([]);
const checkInStatus = ref<UserCheckInStatusResponse | null>(null);
const signingIn = ref(false);
const profile = computed(() => (authStore.currentUser ? mapUserProfile(authStore.currentUser) : null));
const monthDays = computed(() =>
  Array.from({ length: checkInStatus.value?.daysInMonth ?? 0 }, (_, index) => index + 1),
);
const signedDaySet = computed(() => new Set(checkInStatus.value?.signedDays ?? []));
const todayOfMonth = computed(() => checkInStatus.value?.todayDayOfMonth ?? 0);
const checkInMonthLabel = computed(() => {
  if (!checkInStatus.value) {
    return '';
  }
  return `${checkInStatus.value.currentYear}.${String(checkInStatus.value.currentMonth).padStart(2, '0')}`;
});

onMounted(async () => {
  if (!authStore.currentUser) {
    await authStore.fetchCurrentUser();
  }
  const [profiles, status] = await Promise.all([followApi.getMyFollowingProfiles(), userApi.getCheckInStatus()]);
  followingProfiles.value = profiles.map(mapUserProfile).slice(0, 3);
  checkInStatus.value = status;
  if (authStore.currentUser) {
    const posts = await postApi.listByUser(authStore.currentUser.userId, 1, 20);
    // 个人主页也复用 Feed 视图模型，保证同一条内容在不同页面展示口径一致。
    userPosts.value = posts.map((item) => mapPostCardToFeedPost(item, 'FOLLOWING'));
  }
});

async function handleCheckIn() {
  if (!checkInStatus.value || checkInStatus.value.checkedInToday || signingIn.value) {
    return;
  }
  signingIn.value = true;
  try {
    const status = await userApi.checkIn();
    checkInStatus.value = status;
    showSuccessToast(status.rewardGranted ? '签到成功，积分 +1' : '今天已经签过到了');
  } finally {
    signingIn.value = false;
  }
}

async function logout() {
  await authStore.logout();
  await router.push('/login');
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 110px;
  background: linear-gradient(180deg, #fff8f2 0%, #f7faff 100%);
}

.hero {
  margin: 0 0 14px;
  padding: 16px 16px 18px;
  border-radius: 0 0 28px 28px;
  background: linear-gradient(180deg, #ffe5d8 0%, #fff2ea 44%, #fffaf7 100%);
  box-shadow: 0 18px 36px rgba(255, 127, 94, 0.1);
}

.hero-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  color: var(--lf-color-text-primary);
  font-size: 18px;
}

.hero-main {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 16px;
  align-items: center;
  margin-top: 14px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.72);
}

.hero-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero h1 {
  margin: 0;
  font-size: 24px;
}

.level {
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(255, 165, 0, 0.14);
  color: #ff9f1a;
  font-size: 12px;
  font-weight: 600;
}

.hero-copy p {
  margin: 8px 0 0;
  color: var(--lf-color-text-secondary);
  line-height: 1.6;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 18px;
  padding: 16px 10px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
}

.login-region {
  margin-top: 12px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.hero article {
  text-align: center;
}

.hero article strong {
  display: block;
}

.hero article strong {
  font-size: 20px;
}

.hero article span {
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.quick-actions,
.checkin-panel,
.panel,
.posts-panel,
.logout-wrap {
  margin: 0 16px 16px;
  padding: 18px;
  border-radius: 22px;
  background: var(--lf-color-surface);
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  text-align: center;
  font-size: 13px;
}

.checkin-panel {
  background: linear-gradient(135deg, #fff0e5 0%, #fffaf4 56%, #ffffff 100%);
  box-shadow: 0 18px 34px rgba(255, 126, 93, 0.12);
}

.checkin-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.checkin-head strong {
  display: block;
  font-size: 18px;
}

.checkin-head p {
  margin: 6px 0 0;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.checkin-button {
  flex-shrink: 0;
  --van-button-primary-background: #ff7a56;
  --van-button-primary-border-color: #ff7a56;
}

.checkin-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.checkin-summary article {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
}

.checkin-summary span {
  display: block;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.checkin-summary strong {
  display: block;
  margin-top: 8px;
  color: var(--lf-color-text-primary);
  font-size: 22px;
}

.checkin-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-top: 16px;
}

.checkin-day {
  display: grid;
  place-items: center;
  aspect-ratio: 1;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.88);
  color: var(--lf-color-text-secondary);
  font-size: 13px;
  font-weight: 600;
  box-shadow: inset 0 0 0 1px rgba(243, 223, 214, 0.72);
}

.checkin-day--signed {
  background: linear-gradient(180deg, #ff9a70 0%, #ff764d 100%);
  color: #fff;
  box-shadow: 0 10px 18px rgba(255, 118, 77, 0.24);
}

.checkin-day--today {
  outline: 2px solid rgba(255, 122, 86, 0.24);
  outline-offset: 2px;
}

.panel {
  display: grid;
  gap: 14px;
}

.circle-card {
  padding-bottom: 6px;
}

.panel-head,
.item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-head span,
.item span,
.circle-item span {
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.circle-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 16px;
  align-items: start;
}

.circle-item {
  text-align: center;
}

.thumb {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 16px;
  margin-bottom: 8px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.circle-item strong {
  display: block;
  margin-bottom: 4px;
  font-size: 13px;
}

.empty-following {
  grid-column: 1 / -1;
  padding: 18px 0 8px;
  text-align: center;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.posts-panel {
  display: grid;
  gap: 12px;
}

.post-card {
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff9f6 0%, #ffffff 100%);
  border: 1px solid rgba(241, 243, 247, 0.9);
}

.post-topic {
  margin: 0 0 8px;
  color: var(--lf-color-primary);
  font-size: 13px;
  font-weight: 600;
}

.post-content {
  margin: 0;
  color: var(--lf-color-text-primary);
  line-height: 1.7;
}

.poll-card {
  margin-top: 12px;
  padding: 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff7f1 0%, #fff 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 166, 130, 0.22);
}

.poll-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.poll-head strong {
  font-size: 15px;
  line-height: 1.5;
  color: var(--lf-color-text-primary);
}

.poll-badge {
  width: fit-content;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 145, 112, 0.16);
  color: #eb6b47;
  font-size: 12px;
  font-weight: 600;
}

.poll-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.poll-option {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 0 12px;
  border-radius: 14px;
  background: rgba(247, 248, 252, 0.95);
  box-shadow: inset 0 0 0 1px rgba(229, 231, 235, 0.85);
}

.poll-option span {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(255, 145, 112, 0.12);
  color: #eb6b47;
  font-size: 12px;
  font-weight: 700;
}

.poll-option strong {
  font-size: 14px;
  font-weight: 500;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 12px;
}

.post-image {
  aspect-ratio: 1;
  border-radius: 14px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.post-meta,
.empty-posts {
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

.empty-posts {
  padding: 8px 0 4px;
  text-align: center;
}

.logout-wrap {
  background: transparent;
  box-shadow: none;
  padding: 0;
}

.logout-button {
  --van-button-danger-background: #ff6f4d;
  --van-button-danger-border-color: #ff6f4d;
  --van-button-large-height: 48px;
  font-weight: 600;
}
</style>
