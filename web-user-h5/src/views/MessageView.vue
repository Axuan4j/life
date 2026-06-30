<template>
  <div class="page">
    <van-nav-bar title="消息中心" fixed placeholder>
      <template #right>
        <van-button
          plain
          hairline
          size="small"
          round
          class="mark-all-btn"
          :disabled="!notificationStore.hasUnread"
          @click="handleMarkAllRead"
        >
          全部已读
        </van-button>
      </template>
    </van-nav-bar>

    <div class="summary-strip">
      <div>
        <p class="summary-label">未读消息</p>
        <strong>{{ notificationStore.unreadCount }}</strong>
      </div>
      <van-tag :type="notificationStore.streamConnected ? 'success' : 'warning'" round plain>
        {{ notificationStore.streamConnected ? '在线提醒已开启' : '提醒连接中' }}
      </van-tag>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="handleRefresh">
      <div class="content">
        <div v-if="notificationStore.loading && notificationStore.items.length === 0" class="state-card">
          正在加载消息...
        </div>

        <template v-else>
          <div v-if="notificationStore.errorMessage" class="state-card error-card">
            <p>{{ notificationStore.errorMessage }}</p>
            <van-button size="small" round plain @click="handleRefresh">重新加载</van-button>
          </div>

          <van-empty
            v-if="notificationStore.items.length === 0"
            description="还没有新的消息"
            class="message-empty"
          />

          <van-cell-group v-else inset class="message-group">
            <van-cell
              v-for="item in notificationStore.items"
              :key="item.notificationId"
              clickable
              class="message-cell"
              :class="{ unread: !item.read }"
              @click="openNotification(item)"
            >
              <template #title>
                <div class="message-title-row">
                  <div class="message-identity">
                    <div
                      class="message-avatar"
                      :style="
                        item.senderAvatarUrl
                          ? { backgroundImage: `url(${item.senderAvatarUrl})` }
                          : { background: getFallbackAvatar(item.senderName) }
                      "
                    ></div>
                    <div class="message-main">
                      <div class="message-headline">
                        <strong>{{ item.senderName }}</strong>
                        <van-tag plain size="medium" round :type="tagTypeMap[item.notificationType]">
                          {{ tagLabelMap[item.notificationType] }}
                        </van-tag>
                      </div>
                      <p class="message-title">{{ item.title }}</p>
                    </div>
                  </div>
                  <div class="message-side">
                    <span class="message-time">{{ formatRelativeTime(item.createdAt) }}</span>
                    <span v-if="!item.read" class="unread-dot"></span>
                  </div>
                </div>
              </template>
              <template #label>
                <p v-if="item.contentText" class="message-preview">{{ item.contentText }}</p>
              </template>
            </van-cell>
          </van-cell-group>

          <div v-if="notificationStore.items.length > 0 && notificationStore.hasMore" class="load-more-wrap">
            <van-button
              block
              round
              plain
              :loading="notificationStore.loading"
              class="load-more-btn"
              @click="handleLoadMore"
            >
              加载更多
            </van-button>
          </div>
        </template>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import type { UserNotificationResponse } from '../services/api';
import { formatRelativeTime, getFallbackAvatar } from '../services/view-models';
import { useUserNotificationStore } from '../stores/notification';

const router = useRouter();
const notificationStore = useUserNotificationStore();
const refreshing = ref(false);

const tagLabelMap: Record<UserNotificationResponse['notificationType'], string> = {
  LIKE: '点赞',
  COMMENT: '评论',
  REPOST: '转发',
  BROADCAST: '广播',
};

const tagTypeMap: Record<UserNotificationResponse['notificationType'], 'primary' | 'success' | 'warning' | 'danger'> = {
  LIKE: 'danger',
  COMMENT: 'primary',
  REPOST: 'warning',
  BROADCAST: 'success',
};

onMounted(async () => {
  if (notificationStore.items.length > 0) {
    try {
      await notificationStore.fetchUnreadCount();
    } catch {
      // 未读数刷新失败不阻断已有消息列表展示。
    }
    return;
  }
  await handleRefresh();
});

async function handleRefresh() {
  refreshing.value = true;
  try {
    await Promise.allSettled([notificationStore.fetchUnreadCount(), notificationStore.refreshList()]);
  } finally {
    refreshing.value = false;
  }
}

async function handleLoadMore() {
  try {
    await notificationStore.loadMore();
  } catch {
    // 具体错误提示由统一请求层和页面错误卡片共同承担，这里不再重复打断用户。
  }
}

async function handleMarkAllRead() {
  if (!notificationStore.hasUnread) {
    return;
  }
  await notificationStore.markAllRead();
  showSuccessToast('已全部标记为已读');
}

async function openNotification(item: UserNotificationResponse) {
  if (!item.read) {
    try {
      await notificationStore.markRead(item.notificationId);
    } catch {
      return;
    }
  }
  if (item.postId) {
    await router.push(`/posts/${item.postId}`);
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 120px;
  background: #f7f8fa;
}

.page :deep(.van-nav-bar) {
  background: rgba(255, 255, 255, 0.98);
}

.page :deep(.van-nav-bar::after) {
  border-bottom: 1px solid rgba(229, 231, 235, 0.88);
}

.mark-all-btn {
  border-color: rgba(255, 122, 89, 0.24);
  color: var(--lf-color-primary);
}

.summary-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px 12px 0;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff;
}

.summary-label {
  margin: 0 0 6px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
}

.summary-strip strong {
  color: var(--lf-color-text-primary);
  font-size: 22px;
}

.content {
  padding-top: 12px;
}

.state-card {
  margin: 0 12px 10px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff;
  color: var(--lf-color-text-secondary);
  font-size: 13px;
}

.error-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.error-card p {
  margin: 0;
}

.message-empty {
  padding-top: 72px;
}

.message-group {
  overflow: hidden;
  background: transparent;
}

.message-cell {
  position: relative;
  margin-bottom: 10px;
  border-radius: 16px;
  background: #fff;
}

.message-cell.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 18px;
  bottom: 18px;
  width: 3px;
  border-radius: 999px;
  background: var(--lf-color-primary);
}

.message-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.message-identity {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.message-avatar {
  flex: 0 0 auto;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.message-main {
  min-width: 0;
  flex: 1;
}

.message-headline {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.message-headline strong {
  color: var(--lf-color-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.message-title {
  margin: 6px 0 0;
  color: var(--lf-color-text-primary);
  font-size: 14px;
  line-height: 1.5;
}

.message-preview {
  margin: 6px 0 0 50px;
  color: var(--lf-color-text-secondary);
  font-size: 12px;
  line-height: 1.55;
}

.message-side {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex: 0 0 auto;
}

.message-time {
  color: var(--lf-color-text-secondary);
  font-size: 12px;
  line-height: 1;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--lf-color-primary);
}

.load-more-wrap {
  padding: 2px 12px 0;
}

.load-more-btn {
  border-color: rgba(255, 122, 89, 0.18);
  color: var(--lf-color-primary);
}
</style>
