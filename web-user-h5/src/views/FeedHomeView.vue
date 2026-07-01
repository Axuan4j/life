<template>
  <div class="page">
    <div class="topbar">
      <div class="brand-side">
        <span class="brand-badge">LF</span>
        <div class="tabs">
          <button type="button" :class="{ active: activeTab === 'following' }" @click="activeTab = 'following'">关注</button>
          <button type="button" :class="{ active: activeTab === 'recommended' }" @click="activeTab = 'recommended'">推荐</button>
        </div>
      </div>
      <div class="top-actions">
        <div class="ghost">⌕</div>
      </div>
    </div>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list :loading="loading" :finished="!feedStore.hasMore" finished-text="没有更多内容了" @load="onLoad">
        <div v-for="item in visibleItems" :key="item.post.postId" class="card-wrap">
          <FeedPostCard
            :item="item"
            @open="goDetail"
            @like="likePost"
            @comment="commentPost"
            @repost="repostPost"
          />
        </div>
        <div v-if="visibleItems.length === 0 && !loading && !refreshing" class="empty-state">
          当前分组还没有内容
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import FeedPostCard from '../components/FeedPostCard.vue';
import type { EntityId } from '../services/api';
import { useFeedStore } from '../stores/feed';

type FeedTab = 'following' | 'recommended';

const router = useRouter();
const feedStore = useFeedStore();
const loading = ref(false);
const refreshing = ref(false);
const activeTab = ref<FeedTab>('following');
const visibleItems = computed(() =>
  feedStore.items.filter((item) =>
    activeTab.value === 'following' ? item.sourceType === 'FOLLOWING' : item.sourceType === 'RECOMMENDED',
  ),
);

onMounted(async () => {
  if (feedStore.items.length === 0) {
    await onRefresh();
  }
});

async function onRefresh() {
  refreshing.value = true;
  await feedStore.refresh();
  refreshing.value = false;
}

async function onLoad() {
  loading.value = true;
  await feedStore.loadMore();
  loading.value = false;
}

async function goDetail(postId: EntityId) {
  await router.push(`/posts/${postId}`);
}

async function likePost(postId: EntityId) {
  await feedStore.toggleLike(postId);
}

async function commentPost(postId: EntityId) {
  await router.push(`/posts/${postId}?focus=comment`);
}

async function repostPost(postId: EntityId) {
  await feedStore.repost(postId);
  showSuccessToast('转发成功');
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 110px;
  background: linear-gradient(180deg, #fff8f4 0%, #f6f9ff 100%);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px 12px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid rgba(229, 231, 235, 0.6);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand-side {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-badge {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff8e69 0%, #ff5d34 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 12px 22px rgba(255, 95, 61, 0.18);
}

.tabs {
  display: flex;
  gap: 28px;
}

.tabs button {
  position: relative;
  border: none;
  background: transparent;
  font-size: 17px;
  color: var(--lf-color-text-secondary);
}

.tabs .active {
  color: var(--lf-color-text-primary);
  font-weight: 700;
}

.tabs .active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -8px;
  width: 24px;
  height: 3px;
  margin-left: -12px;
  border-radius: 999px;
  background: linear-gradient(90deg, #ff8b68 0%, #ff5f3d 100%);
}

.top-actions {
  display: flex;
  gap: 10px;
}

.ghost {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba(248, 116, 78, 0.08);
  text-align: right;
  font-size: 19px;
  color: var(--lf-color-text-primary);
}

.card-wrap {
  margin: 12px 12px 0;
}

.empty-state {
  padding: 48px 16px 8px;
  text-align: center;
  color: var(--lf-color-text-secondary);
  font-size: 14px;
}
</style>
