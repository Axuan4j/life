<template>
  <div class="page">
    <van-sticky>
      <div class="top-shell">
        <van-nav-bar left-arrow title="发现结果" @click-left="goBack" />
        <van-search
          v-model="searchKeyword"
          shape="round"
          placeholder="搜索热词、话题或关键词"
          @search="submitKeyword"
        />
      </div>
    </van-sticky>

    <div v-if="discoverStore.resultHeader" class="result-hero">
      <p class="result-eyebrow">{{ discoverStore.resultHeader.resultType === 'TOPIC' ? 'Topic' : 'Keyword' }}</p>
      <h1>{{ discoverStore.resultHeader.title }}</h1>
      <p class="result-subtitle">{{ discoverStore.resultHeader.subtitle }}</p>
      <span class="result-count">{{ discoverStore.resultHeader.totalCountText }}</span>
    </div>

    <van-tabs v-model:active="activeSort" sticky offset-top="102" @change="changeSort">
      <van-tab title="综合" name="COMPOSITE" />
      <van-tab title="最新" name="LATEST" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="refreshCurrentResult">
      <van-list :loading="loading" :finished="!discoverStore.resultHasMore" finished-text="没有更多内容了" @load="onLoad">
        <div v-for="item in discoverStore.resultItems" :key="item.post.postId" class="card-wrap">
          <FeedPostCard
            :item="item"
            :show-source-badge="false"
            @open="goDetail"
            @like="likePost"
            @comment="commentPost"
            @repost="repostPost"
          />
        </div>
        <div v-if="discoverStore.resultItems.length === 0 && !loading && !refreshing" class="empty-wrap">
          <van-empty description="暂时还没有找到相关内容" />
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import FeedPostCard from '../components/FeedPostCard.vue';
import { postApi, type DiscoverResultSort, type DiscoverResultType, type EntityId } from '../services/api';
import { useDiscoverStore } from '../stores/discover';

const route = useRoute();
const router = useRouter();
const discoverStore = useDiscoverStore();

const loading = ref(false);
const refreshing = ref(false);
const searchKeyword = ref('');
const activeSort = ref<DiscoverResultSort>('COMPOSITE');

onMounted(async () => {
  await refreshFromRoute();
});

watch(
  () => route.fullPath,
  async () => {
    await refreshFromRoute();
  },
);

async function refreshFromRoute() {
  const type = normalizeType(route.query.type);
  if (!type) {
    await router.replace('/discover');
    return;
  }
  const nextSort = normalizeSort(route.query.sort);
  activeSort.value = nextSort;
  const topicKey = typeof route.query.topicKey === 'string' ? route.query.topicKey : undefined;
  const keyword = typeof route.query.keyword === 'string' ? route.query.keyword : undefined;
  searchKeyword.value = keyword ?? discoverStore.resultHeader?.queryValue ?? '';
  await discoverStore.refreshResults(
    {
      type,
      topicKey,
      keyword,
    },
    nextSort,
  );
}

async function refreshCurrentResult() {
  refreshing.value = true;
  await refreshFromRoute();
  refreshing.value = false;
}

async function onLoad() {
  loading.value = true;
  await discoverStore.loadMoreResults();
  loading.value = false;
}

async function changeSort(name: string | number) {
  const sort = name === 'LATEST' ? 'LATEST' : 'COMPOSITE';
  await router.replace({
    path: '/discover/result',
    query: {
      ...route.query,
      sort,
    },
  });
}

async function submitKeyword() {
  const normalized = searchKeyword.value.trim();
  if (!normalized) {
    return;
  }
  await router.replace({
    path: '/discover/result',
    query: {
      type: 'KEYWORD',
      keyword: normalized,
      sort: activeSort.value,
    },
  });
}

async function goDetail(postId: EntityId) {
  await router.push(`/posts/${postId}`);
}

async function likePost(postId: EntityId) {
  const interaction = await postApi.toggleLike(postId);
  discoverStore.updateResultPostCounters(postId, interaction);
}

async function commentPost(postId: EntityId) {
  await router.push(`/posts/${postId}?focus=comment`);
}

async function repostPost(postId: EntityId) {
  const interaction = await postApi.repost(postId);
  discoverStore.updateResultPostCounters(postId, interaction);
  showSuccessToast('转发成功');
}

async function goBack() {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  await router.replace('/discover');
}

function normalizeType(value: unknown): DiscoverResultType | null {
  return value === 'TOPIC' || value === 'KEYWORD' ? value : null;
}

function normalizeSort(value: unknown): DiscoverResultSort {
  return value === 'LATEST' ? 'LATEST' : 'COMPOSITE';
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 32px;
  background: linear-gradient(180deg, #fff8f2 0%, #f7faff 100%);
}

.top-shell {
  background: rgba(255, 250, 247, 0.96);
  backdrop-filter: blur(10px);
}

.result-hero {
  margin: 12px 12px 0;
  padding: 18px 16px;
  border-radius: 24px;
  background: linear-gradient(180deg, #fff0e7 0%, #fff9f4 100%);
  box-shadow: 0 14px 30px rgba(26, 36, 56, 0.06);
}

.result-eyebrow {
  margin: 0;
  color: var(--lf-color-primary);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.result-hero h1 {
  margin: 8px 0 0;
  font-size: 26px;
}

.result-subtitle {
  margin: 10px 0 0;
  color: var(--lf-color-text-secondary);
  line-height: 1.6;
  font-size: 14px;
}

.result-count {
  display: inline-block;
  margin-top: 14px;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.card-wrap {
  margin: 12px 12px 0;
}

.empty-wrap {
  padding-top: 48px;
}
</style>
