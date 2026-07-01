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
    <div v-if="item.post.poll" class="poll-card">
      <div class="poll-head">
        <span class="poll-badge">投票</span>
        <strong>{{ item.post.poll.question }}</strong>
      </div>
      <div class="poll-options">
        <div v-for="(option, index) in item.post.poll.options" :key="`${item.post.postId}-poll-${index}`" class="poll-option">
          <span>{{ index + 1 }}</span>
          <strong>{{ option }}</strong>
        </div>
      </div>
    </div>
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
import type { EntityId } from '../services/api';
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
  (e: 'open', postId: EntityId): void;
  (e: 'like', postId: EntityId): void;
  (e: 'comment', postId: EntityId): void;
  (e: 'repost', postId: EntityId): void;
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

.poll-card {
  margin: 0 0 12px;
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
