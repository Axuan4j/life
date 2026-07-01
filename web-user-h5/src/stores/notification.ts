import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { http } from '../services/http';
import {
  notificationApi,
  type EntityId,
  type NotificationStreamEventResponse,
  type UserNotificationResponse,
} from '../services/api';
import { useUserAuthStore } from './auth';

const DEFAULT_PAGE_SIZE = 20;
const RECONNECT_DELAY_MS = 3000;

export const useUserNotificationStore = defineStore('user-notification', () => {
  const authStore = useUserAuthStore();
  const items = ref<UserNotificationResponse[]>([]);
  const unreadCount = ref(0);
  const pageNo = ref(1);
  const total = ref(0);
  const loading = ref(false);
  const finished = ref(false);
  const streamConnected = ref(false);
  const errorMessage = ref('');

  let eventSource: EventSource | null = null;
  let reconnectTimer: number | null = null;

  const hasUnread = computed(() => unreadCount.value > 0);
  const hasMore = computed(() => !finished.value);

  async function fetchUnreadCount() {
    if (!authStore.accessToken) {
      unreadCount.value = 0;
      return 0;
    }
    const response = await notificationApi.getUnreadCount();
    unreadCount.value = response.unreadCount;
    return unreadCount.value;
  }

  async function refreshList() {
    return fetchPage(1, true);
  }

  async function loadMore() {
    if (loading.value || finished.value) {
      return;
    }
    return fetchPage(pageNo.value, false);
  }

  async function markRead(notificationId: EntityId) {
    const target = items.value.find((item) => item.notificationId === notificationId);
    if (target?.read) {
      return;
    }
    await notificationApi.markRead(notificationId);
    if (target) {
      target.read = true;
    }
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  }

  async function markAllRead() {
    await notificationApi.markAllRead();
    items.value = items.value.map((item) => ({
      ...item,
      read: true,
    }));
    unreadCount.value = 0;
  }

  function connectStream() {
    if (!authStore.accessToken) {
      return;
    }
    disconnectStream();

    const streamUrl = new URL('/api/notifications/stream', http.defaults.baseURL ?? window.location.origin);
    streamUrl.searchParams.set('accessToken', authStore.accessToken);

    const nextEventSource = new EventSource(streamUrl.toString());
    eventSource = nextEventSource;

    nextEventSource.onopen = () => {
      streamConnected.value = true;
    };

    nextEventSource.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data) as NotificationStreamEventResponse;
        handleStreamEvent(payload);
      } catch {
        // 单条 SSE 消息解析失败时不直接熔断整条连接，尽量保证后续推送还能继续消费。
      }
    };

    nextEventSource.onerror = () => {
      streamConnected.value = false;
      if (eventSource === nextEventSource) {
        nextEventSource.close();
        eventSource = null;
      }
      scheduleReconnect();
    };
  }

  function disconnectStream() {
    if (eventSource) {
      eventSource.close();
      eventSource = null;
    }
    if (reconnectTimer !== null) {
      window.clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    streamConnected.value = false;
  }

  function reset() {
    disconnectStream();
    items.value = [];
    unreadCount.value = 0;
    pageNo.value = 1;
    total.value = 0;
    loading.value = false;
    finished.value = false;
    errorMessage.value = '';
  }

  function handleStreamEvent(payload: NotificationStreamEventResponse) {
    unreadCount.value = payload.unreadCount ?? unreadCount.value;
    if (payload.eventType === 'NOTIFICATION_CREATED' && payload.notification) {
      total.value += 1;
      finished.value = false;
      items.value = [payload.notification, ...items.value.filter((item) => item.notificationId !== payload.notification?.notificationId)];
    }
  }

  function scheduleReconnect() {
    if (reconnectTimer !== null || !authStore.accessToken) {
      return;
    }
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null;
      if (authStore.accessToken) {
        connectStream();
      }
    }, RECONNECT_DELAY_MS);
  }

  function mergeUnique(notificationItems: UserNotificationResponse[]) {
    const seen = new Set<EntityId>();
    return notificationItems.filter((item) => {
      if (seen.has(item.notificationId)) {
        return false;
      }
      seen.add(item.notificationId);
      return true;
    });
  }

  async function fetchPage(targetPageNo: number, replaceItems: boolean) {
    loading.value = true;
    errorMessage.value = '';
    try {
      const page = await notificationApi.list(targetPageNo, DEFAULT_PAGE_SIZE);
      total.value = page.total;
      items.value = replaceItems ? page.items : mergeUnique([...items.value, ...page.items]);
      pageNo.value = targetPageNo + 1;
      finished.value = items.value.length >= total.value || page.items.length < DEFAULT_PAGE_SIZE;
      return page;
    } catch (error) {
      errorMessage.value = '消息加载失败，请稍后重试';
      throw error;
    } finally {
      loading.value = false;
    }
  }

  return {
    items,
    unreadCount,
    hasUnread,
    hasMore,
    loading,
    finished,
    streamConnected,
    errorMessage,
    fetchUnreadCount,
    refreshList,
    loadMore,
    markRead,
    markAllRead,
    connectStream,
    disconnectStream,
    reset,
  };
});
