<template>
  <div class="page">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <header class="hero">
        <div class="hero-copy">
          <p class="eyebrow">Discover</p>
          <h1>发现</h1>
          <p class="hero-desc">从热搜话题到优质作者，快速找到值得继续停留的内容。</p>
        </div>
        <div class="hero-mark">热榜</div>
      </header>

      <van-sticky>
        <section class="sticky-search">
          <van-search
            v-model="searchKeyword"
            shape="round"
            show-action
            placeholder="搜索热词、话题或关键词"
            @focus="searchPanelVisible = true"
            @search="submitKeyword()"
          >
            <template #action>
              <span class="search-action" @click="searchPanelVisible = false">收起</span>
            </template>
          </van-search>
          <div v-if="searchPanelVisible" class="search-panel">
            <div v-if="discoverStore.recentSearches.length > 0" class="search-block">
              <div class="search-head">
                <strong>最近搜索</strong>
                <button type="button" class="clear-btn" @click="discoverStore.clearRecentSearches()">清空</button>
              </div>
              <div class="tag-list">
                <van-tag
                  v-for="keyword in discoverStore.recentSearches"
                  :key="keyword"
                  plain
                  round
                  type="primary"
                  class="tag-item"
                  @click="submitKeyword(keyword)"
                >
                  {{ keyword }}
                </van-tag>
              </div>
            </div>
            <div class="search-block">
              <div class="search-head">
                <strong>热门搜索</strong>
              </div>
              <div class="search-suggest-list">
                <button
                  v-for="item in suggestionKeywords"
                  :key="item.keyword"
                  type="button"
                  class="suggest-row"
                  @click="submitKeyword(item.keyword)"
                >
                  <span class="suggest-rank">{{ item.rank }}</span>
                  <span class="suggest-title">{{ item.title }}</span>
                  <span class="suggest-trend">{{ item.trendLabel }}</span>
                </button>
              </div>
            </div>
          </div>
        </section>
      </van-sticky>

      <section v-if="discoverStore.home" class="content">
        <section class="section">
          <div class="section-head">
            <div>
              <h2>热搜榜</h2>
              <p>大家正在关注的关键词</p>
            </div>
          </div>
          <div class="hot-top-grid">
            <button
              v-for="item in featuredKeywords"
              :key="item.keyword"
              type="button"
              class="hot-top-card"
              :class="`rank-${item.rank}`"
              @click="openHotKeyword(item.keyword)"
            >
              <span class="rank-badge">TOP {{ item.rank }}</span>
              <strong>{{ item.title }}</strong>
              <span class="trend">{{ item.trendLabel }}</span>
              <span class="heat">{{ item.heatLabel }}</span>
            </button>
          </div>
          <div class="hot-list">
            <button
              v-for="item in trailingKeywords"
              :key="item.keyword"
              type="button"
              class="hot-row"
              @click="openHotKeyword(item.keyword)"
            >
              <span class="hot-rank">{{ item.rank }}</span>
              <span class="hot-title">{{ item.title }}</span>
              <span class="hot-heat">{{ item.heatLabel }}</span>
            </button>
          </div>
        </section>

        <section class="section">
          <div class="section-head">
            <div>
              <h2>话题广场</h2>
              <p>进入感兴趣的主题内容流</p>
            </div>
          </div>
          <div class="topic-grid">
            <button
              v-for="topic in discoverStore.home.topicSquare"
              :key="topic.topicKey"
              type="button"
              class="topic-card"
              :class="topic.coverStyle"
              @click="openTopic(topic.topicKey)"
            >
              <strong># {{ topic.title }}</strong>
              <p>{{ topic.summary }}</p>
              <span>{{ topic.discussionText }}</span>
            </button>
          </div>
        </section>

        <section class="section">
          <div class="section-head">
            <div>
              <h2>推荐作者</h2>
              <p>关注后会优先进入你的首页内容流</p>
            </div>
          </div>
          <div class="author-list">
            <article
              v-for="author in discoverStore.home.recommendedAuthors"
              :key="author.userId"
              class="author-card"
              @click="goAuthorProfile(author.userId)"
            >
              <div
                class="author-avatar"
                :style="
                  author.avatarUrl
                    ? { backgroundImage: `url(${author.avatarUrl})` }
                    : { background: getFallbackAvatar(author.displayName) }
                "
              ></div>
              <div class="author-copy">
                <div class="author-head">
                  <strong>{{ author.displayName }}</strong>
                  <van-tag plain round type="warning">{{ author.reason }}</van-tag>
                </div>
                <p class="author-username">@{{ author.username }}</p>
                <p class="author-bio">{{ author.bio }}</p>
                <p class="author-meta">{{ author.followerText }}</p>
              </div>
              <van-button
                round
                size="small"
                :type="author.following ? 'default' : 'primary'"
                :loading="followLoadingUserId === author.userId"
                @click.stop="toggleFollow(author.userId, author.following)"
              >
                {{ author.following ? '已关注' : '关注' }}
              </van-button>
            </article>
          </div>
        </section>
      </section>

      <div v-else class="empty-wrap">
        <van-empty description="发现页正在准备内容" />
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { followApi, type EntityId } from '../services/api';
import { getFallbackAvatar } from '../services/view-models';
import { useDiscoverStore } from '../stores/discover';

const router = useRouter();
const discoverStore = useDiscoverStore();

const refreshing = ref(false);
const searchPanelVisible = ref(false);
const searchKeyword = ref('');
const followLoadingUserId = ref<EntityId | null>(null);

const featuredKeywords = computed(() => discoverStore.home?.hotKeywords.slice(0, 3) ?? []);
const trailingKeywords = computed(() => discoverStore.home?.hotKeywords.slice(3, 10) ?? []);
const suggestionKeywords = computed(() => discoverStore.home?.hotKeywords.slice(0, 6) ?? []);

onMounted(async () => {
  if (!discoverStore.homeLoaded) {
    await onRefresh();
  }
});

async function onRefresh() {
  refreshing.value = true;
  await discoverStore.loadHome(true);
  refreshing.value = false;
}

async function submitKeyword(nextKeyword = searchKeyword.value) {
  const normalized = nextKeyword.trim();
  if (!normalized) {
    return;
  }
  searchKeyword.value = normalized;
  searchPanelVisible.value = false;
  await router.push({
    path: '/discover/result',
    query: {
      type: 'KEYWORD',
      keyword: normalized,
    },
  });
}

async function openHotKeyword(keyword: string) {
  await submitKeyword(keyword);
}

async function openTopic(topicKey: string) {
  searchPanelVisible.value = false;
  await router.push({
    path: '/discover/result',
    query: {
      type: 'TOPIC',
      topicKey,
    },
  });
}

async function goAuthorProfile(userId: EntityId) {
  await router.push(`/users/${userId}`);
}

async function toggleFollow(userId: EntityId, following: boolean) {
  followLoadingUserId.value = userId;
  try {
    if (following) {
      await followApi.unfollow(userId);
      discoverStore.updateRecommendedAuthorFollowState(userId, false);
      showSuccessToast('已取消关注');
    } else {
      await followApi.follow(userId);
      discoverStore.updateRecommendedAuthorFollowState(userId, true);
      showSuccessToast('关注成功');
    }
  } finally {
    followLoadingUserId.value = null;
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 110px;
  background: linear-gradient(180deg, #fff8f2 0%, #f7faff 100%);
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 16px 12px;
}

.eyebrow {
  margin: 0;
  color: var(--lf-color-primary);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero h1 {
  margin: 6px 0 0;
  font-size: 30px;
}

.hero-desc {
  margin: 10px 0 0;
  color: var(--lf-color-text-secondary);
  line-height: 1.6;
  font-size: 14px;
}

.hero-mark {
  flex-shrink: 0;
  align-self: flex-start;
  padding: 8px 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff8f70 0%, #ff5e39 100%);
  box-shadow: 0 12px 24px rgba(255, 95, 61, 0.2);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.sticky-search {
  padding: 0 12px 8px;
  background: rgba(255, 250, 247, 0.96);
  backdrop-filter: blur(10px);
  z-index: 5;
}

.search-action {
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.search-panel {
  margin-top: 10px;
  padding: 14px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 18px 34px rgba(26, 36, 56, 0.08);
}

.search-block + .search-block {
  margin-top: 14px;
}

.search-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-head strong {
  font-size: 14px;
}

.clear-btn {
  border: none;
  background: transparent;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.tag-item {
  cursor: pointer;
}

.search-suggest-list {
  display: grid;
  gap: 10px;
  margin-top: 10px;
}

.suggest-row {
  display: grid;
  grid-template-columns: 24px 1fr auto;
  gap: 10px;
  align-items: center;
  border: none;
  background: transparent;
  text-align: left;
  padding: 0;
}

.suggest-rank {
  color: var(--lf-color-primary);
  font-weight: 700;
}

.suggest-title {
  color: var(--lf-color-text-primary);
  font-size: 14px;
}

.suggest-trend {
  color: #ff7a59;
  font-size: 12px;
}

.content {
  padding: 0 12px 8px;
}

.section + .section {
  margin-top: 18px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 12px;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
}

.section-head p {
  margin: 6px 0 0;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.hot-top-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.hot-top-card,
.hot-row,
.topic-card {
  border: none;
  text-align: left;
}

.hot-top-card {
  min-height: 126px;
  padding: 16px 14px;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.rank-1 {
  background: linear-gradient(180deg, #fff0e6 0%, #ffe2d3 100%);
}

.rank-2 {
  background: linear-gradient(180deg, #f2f6ff 0%, #e4eeff 100%);
}

.rank-3 {
  background: linear-gradient(180deg, #effbf3 0%, #e0f7e8 100%);
}

.rank-badge,
.trend,
.heat {
  display: block;
}

.rank-badge {
  color: var(--lf-color-primary);
  font-size: 11px;
  font-weight: 700;
}

.hot-top-card strong {
  display: block;
  margin-top: 12px;
  font-size: 16px;
  line-height: 1.4;
}

.trend {
  margin-top: 12px;
  color: #ff7a59;
  font-size: 12px;
}

.heat {
  margin-top: 6px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.hot-list {
  margin-top: 12px;
  padding: 8px 14px;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.hot-row {
  display: grid;
  grid-template-columns: 26px 1fr auto;
  gap: 10px;
  align-items: center;
  width: 100%;
  padding: 12px 0;
  background: transparent;
}

.hot-row + .hot-row {
  border-top: 1px solid rgba(229, 231, 235, 0.72);
}

.hot-rank {
  color: var(--lf-color-primary);
  font-weight: 700;
}

.hot-title {
  color: var(--lf-color-text-primary);
  font-size: 14px;
}

.hot-heat {
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.topic-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.topic-card {
  min-height: 148px;
  padding: 18px 16px;
  border-radius: 22px;
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.topic-card strong {
  display: block;
  font-size: 16px;
}

.topic-card p {
  margin: 12px 0 10px;
  color: #334155;
  line-height: 1.6;
  font-size: 13px;
}

.topic-card span {
  color: #64748b;
  font-size: 12px;
}

.warm {
  background: linear-gradient(180deg, #fff4ea 0%, #ffe8da 100%);
}

.blue {
  background: linear-gradient(180deg, #eef6ff 0%, #deeeff 100%);
}

.green {
  background: linear-gradient(180deg, #effbf3 0%, #ddf5e7 100%);
}

.peach {
  background: linear-gradient(180deg, #fff6f1 0%, #ffece4 100%);
}

.author-list {
  display: grid;
  gap: 12px;
}

.author-card {
  display: grid;
  grid-template-columns: 52px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 16px;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.author-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.author-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.author-head strong {
  font-size: 15px;
}

.author-username,
.author-bio,
.author-meta {
  margin: 0;
}

.author-username {
  padding-top: 4px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.author-bio {
  padding-top: 6px;
  color: #334155;
  line-height: 1.5;
  font-size: 13px;
}

.author-meta {
  padding-top: 8px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.empty-wrap {
  padding: 56px 0 0;
}
</style>
