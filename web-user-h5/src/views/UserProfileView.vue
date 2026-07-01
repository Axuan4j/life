<template>
  <div class="page">
    <van-nav-bar left-arrow title="用户主页" @click-left="goBack" />

    <div v-if="profile" class="hero">
      <div class="hero-main">
        <div
          class="avatar"
          :style="
            profile.avatarUrl
              ? { backgroundImage: `url(${profile.avatarUrl})` }
              : { background: getFallbackAvatar(profile.displayName) }
          "
        ></div>
        <div class="hero-copy">
          <div class="hero-line">
            <h1>{{ profile.displayName }}</h1>
            <span class="level">@{{ profile.username }}</span>
          </div>
          <p>{{ profile.bio }}</p>
          <div class="hero-actions">
            <span class="region">最近登录地：{{ profile.lastLoginRegion || '未知' }}</span>
            <van-button
              v-if="!isSelfProfile"
              round
              size="small"
              :type="following ? 'default' : 'primary'"
              :loading="followSubmitting"
              @click="toggleFollow"
            >
              {{ following ? '已关注' : '关注' }}
            </van-button>
          </div>
        </div>
      </div>
      <div class="hero-stats">
        <article>
          <strong>{{ profile.postCount }}</strong>
          <span>帖子</span>
        </article>
        <article>
          <strong>{{ profile.followingCount }}</strong>
          <span>关注</span>
        </article>
        <article>
          <strong>{{ profile.followerCount }}</strong>
          <span>粉丝</span>
        </article>
        <article>
          <strong>{{ profile.likedCount }}</strong>
          <span>获赞</span>
        </article>
      </div>
    </div>

    <section class="posts-panel">
      <div class="panel-head">
        <strong>TA 的内容</strong>
        <span>{{ postItems.length }} 条</span>
      </div>
      <div v-if="postItems.length > 0" class="post-list">
        <FeedPostCard
          v-for="item in postItems"
          :key="item.post.postId"
          :item="item"
          :show-source-badge="false"
          @open="goDetail"
          @like="likePost"
          @comment="commentPost"
          @repost="repostPost"
        />
      </div>
      <van-empty v-else description="还没有公开内容" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import FeedPostCard from '../components/FeedPostCard.vue';
import { followApi, postApi, userApi, type EntityId } from '../services/api';
import { getFallbackAvatar, mapPostCardToFeedPost, mapUserProfile } from '../services/view-models';
import { useUserAuthStore } from '../stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useUserAuthStore();

const profile = ref<ReturnType<typeof mapUserProfile> | null>(null);
const posts = ref<ReturnType<typeof mapPostCardToFeedPost>[]>([]);
const following = ref(false);
const followSubmitting = ref(false);

const viewedUserId = computed(() => resolveRouteEntityId(route.params.userId));
const isSelfProfile = computed(() => authStore.currentUser?.userId === viewedUserId.value);
const postItems = computed(() => posts.value.map((post) => ({ sourceType: 'RECOMMENDED' as const, post })));

onMounted(async () => {
  await loadUserProfile();
});

watch(
  () => route.params.userId,
  async () => {
    await loadUserProfile();
  },
);

async function loadUserProfile() {
  const userId = viewedUserId.value;
  if (!userId) {
    await router.replace('/discover');
    return;
  }
  if (!authStore.currentUser) {
    await authStore.fetchCurrentUser();
  }
  const [profileResponse, postsResponse, followStatus] = await Promise.all([
    userApi.getProfile(userId),
    postApi.listByUser(userId, 1, 20),
    isSelfProfile.value ? Promise.resolve({ targetUserId: userId, following: false }) : followApi.getStatus(userId),
  ]);
  profile.value = mapUserProfile(profileResponse);
  posts.value = postsResponse.map((item) => mapPostCardToFeedPost(item, 'RECOMMENDED'));
  following.value = followStatus.following;
}

async function toggleFollow() {
  if (!profile.value || isSelfProfile.value) {
    return;
  }
  followSubmitting.value = true;
  try {
    if (following.value) {
      await followApi.unfollow(profile.value.userId);
      following.value = false;
      profile.value = {
        ...profile.value,
        followerCount: Math.max(0, profile.value.followerCount - 1),
      };
      showSuccessToast('已取消关注');
    } else {
      await followApi.follow(profile.value.userId);
      following.value = true;
      profile.value = {
        ...profile.value,
        followerCount: profile.value.followerCount + 1,
      };
      showSuccessToast('关注成功');
    }
  } finally {
    followSubmitting.value = false;
  }
}

async function likePost(postId: EntityId) {
  const interaction = await postApi.toggleLike(postId);
  updatePostCounters(postId, interaction.likeCount, interaction.commentCount, interaction.repostCount);
}

async function commentPost(postId: EntityId) {
  await router.push(`/posts/${postId}?focus=comment`);
}

async function repostPost(postId: EntityId) {
  const interaction = await postApi.repost(postId);
  updatePostCounters(postId, interaction.likeCount, interaction.commentCount, interaction.repostCount);
  showSuccessToast('转发成功');
}

function updatePostCounters(postId: EntityId, likeCount: number, commentCount: number, repostCount: number) {
  posts.value = posts.value.map((item) =>
    item.postId === postId
      ? {
          ...item,
          likeCount,
          commentCount,
          repostCount,
        }
      : item,
  );
}

async function goDetail(postId: EntityId) {
  await router.push(`/posts/${postId}`);
}

function resolveRouteEntityId(value: string | string[] | undefined) {
  return typeof value === 'string' ? value : value?.[0] ?? '';
}

async function goBack() {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  await router.replace('/discover');
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 32px;
  background: linear-gradient(180deg, #fff8f2 0%, #f7faff 100%);
}

.hero {
  margin: 12px 12px 16px;
  padding: 18px 16px;
  border-radius: 24px;
  background: linear-gradient(180deg, #ffe5d8 0%, #fff2ea 44%, #fffaf7 100%);
  box-shadow: 0 18px 36px rgba(255, 127, 94, 0.1);
}

.hero-main {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 16px;
  align-items: center;
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

.hero-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
}

.region {
  color: var(--lf-color-text-secondary);
  font-size: 12px;
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

.hero-stats article {
  text-align: center;
}

.hero-stats strong {
  display: block;
  font-size: 20px;
}

.hero-stats span {
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.posts-panel {
  margin: 0 12px 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-head strong {
  font-size: 20px;
}

.panel-head span {
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.post-list {
  display: grid;
  gap: 12px;
}
</style>
