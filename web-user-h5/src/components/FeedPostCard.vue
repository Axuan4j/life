<template>
  <article class="card" @click="emit('open', item.post.postId)">
    <div class="meta">
      <div class="author">
        <div
          class="avatar"
          :style="
            item.post.authorAvatarUrl
              ? { backgroundImage: `url(${item.post.authorAvatarUrl})` }
              : { background: getFallbackAvatar(item.post.authorName) }
          "
        ></div>
        <div>
          <div class="author-line">
            <strong>{{ item.post.authorName }}</strong>
            <span v-if="showSourceBadge" class="author-badge">{{ item.post.authorBadge }}</span>
          </div>
          <p class="author-meta">
            <span>{{ item.post.publishedAt }}</span>
            <span class="inline-meta">
              <van-icon name="location-o" class="mini-icon" />
              <span>{{ item.post.ipRegion }}</span>
            </span>
          </p>
        </div>
      </div>
      <span class="more">···</span>
    </div>
    <p v-if="item.post.topic" class="topic"># {{ item.post.topic }}</p>
    <p v-if="item.post.displayContentText" class="content">{{ item.post.displayContentText }}</p>
    <div v-if="item.post.images.length > 0" class="image-grid">
      <div
        v-for="image in item.post.images"
        :key="image.id"
        class="image-card"
        :style="image.url ? { backgroundImage: `url(${image.url})` } : { background: image.palette }"
      ></div>
    </div>
    <div class="subline">
      <span>{{ item.post.location }}</span>
      <span v-if="showSourceBadge" class="badge" :class="item.sourceType.toLowerCase()">
        {{ item.sourceType === 'FOLLOWING' ? '关注' : '推荐' }}
      </span>
    </div>
    <div class="stats">
      <button type="button" class="stat-btn" @click.stop="emit('like', item.post.postId)">
        <van-icon name="like-o" class="stat-icon" />
        <span>{{ item.post.likeCount }}</span>
      </button>
      <button type="button" class="stat-btn" @click.stop="emit('comment', item.post.postId)">
        <van-icon name="chat-o" class="stat-icon" />
        <span>{{ item.post.commentCount }}</span>
      </button>
      <button type="button" class="stat-btn" @click.stop="emit('repost', item.post.postId)">
        <van-icon name="replay" class="stat-icon" />
        <span>{{ item.post.repostCount }}</span>
      </button>
    </div>
  </article>
</template>

<script setup lang="ts">
import { getFallbackAvatar, type FeedItemViewModel } from '../services/view-models';

withDefaults(
  defineProps<{
    item: FeedItemViewModel;
    showSourceBadge?: boolean;
  }>(),
  {
    showSourceBadge: true,
  },
);

const emit = defineEmits<{
  (e: 'open', postId: number): void;
  (e: 'like', postId: number): void;
  (e: 'comment', postId: number): void;
  (e: 'repost', postId: number): void;
}>();
</script>

<style scoped>
.card {
  padding: 16px 16px 14px;
  border-radius: 22px;
  background: var(--lf-color-surface);
  box-shadow: 0 12px 30px rgba(26, 36, 56, 0.06);
  cursor: pointer;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  box-shadow: inset 0 0 0 2px rgba(255, 255, 255, 0.7);
}

.author-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta strong {
  font-size: 15px;
}

.author-badge {
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(255, 160, 122, 0.18);
  color: #f06d47;
  font-size: 11px;
}

.meta p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--lf-color-text-secondary);
}

.author-meta,
.inline-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.author-meta {
  flex-wrap: wrap;
}

.mini-icon {
  font-size: 11px;
  line-height: 1;
}

.more {
  color: #9ca3af;
  font-size: 22px;
  line-height: 1;
}

.topic {
  margin: 14px 0 8px;
  color: var(--lf-color-primary);
  font-weight: 600;
  font-size: 14px;
}

.content {
  margin: 0 0 12px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--lf-color-text-primary);
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.image-card {
  aspect-ratio: 1;
  border-radius: 14px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.subline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.badge {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
}

.following {
  color: var(--lf-color-secondary);
  background: rgba(45, 127, 249, 0.1);
}

.recommended {
  color: #f06d47;
  background: rgba(255, 145, 112, 0.16);
}

.stats {
  display: flex;
  gap: 8px;
  padding-top: 2px;
}

.stat-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 38px;
  border: none;
  border-radius: 12px;
  background: rgba(247, 248, 252, 0.95);
  box-shadow: inset 0 0 0 1px rgba(229, 231, 235, 0.9);
  color: var(--lf-color-text-secondary);
  font: inherit;
}

.stat-icon {
  font-size: 14px;
  line-height: 1;
}

.stat-btn :deep(.van-icon) {
  font-size: 15px;
}
</style>
