import { ref } from 'vue';
import { defineStore } from 'pinia';
import { feedApi, postApi } from '../services/api';
import { mapFeedItem, mapPostCardToFeedPost, type FeedItemViewModel } from '../services/view-models';

export interface FeedPost {
  postId: number;
  authorId: number;
  authorName: string;
  authorBadge: string;
  contentText: string;
  displayContentText: string;
  publishedAt: string;
  topic: string;
  location: string;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  images: Array<{ id: number; palette: string }>;
}

export interface FeedItem {
  sourceType: 'FOLLOWING' | 'RECOMMENDED';
  post: FeedPost;
}

export const useFeedStore = defineStore('feed', () => {
  const items = ref<FeedItemViewModel[]>([]);
  const nextCursor = ref('');
  const hasMore = ref(true);
  const initialized = ref(false);

  async function ensureInitialized() {
    if (!initialized.value) {
      await refresh();
    }
  }

  async function refresh() {
    const page = await feedApi.getHomeFeed('', 10);
    items.value = page.items.map(mapFeedItem);
    nextCursor.value = page.nextCursor;
    hasMore.value = page.hasMore;
    initialized.value = true;
  }

  async function loadMore() {
    if (!hasMore.value) {
      return;
    }
    const page = await feedApi.getHomeFeed(nextCursor.value, 10);
    items.value = [...items.value, ...page.items.map(mapFeedItem)];
    nextCursor.value = page.nextCursor;
    hasMore.value = page.hasMore;
  }

  async function publishPost(contentText: string) {
    const created = await postApi.create({
      contentText,
      medias: [],
    });
    items.value.unshift({
      sourceType: 'FOLLOWING',
      post: mapPostCardToFeedPost(created, 'FOLLOWING'),
    });
  }

  async function toggleLike(postId: number) {
    const interaction = await postApi.toggleLike(postId);
    updatePostCounters(postId, interaction.likeCount, interaction.commentCount, interaction.repostCount);
    return interaction;
  }

  async function repost(postId: number) {
    const interaction = await postApi.repost(postId);
    updatePostCounters(postId, interaction.likeCount, interaction.commentCount, interaction.repostCount);
    await refresh();
    return interaction;
  }

  function updatePostCounters(postId: number, likeCount: number, commentCount: number, repostCount: number) {
    items.value = items.value.map((item) =>
      item.post.postId === postId
        ? {
            ...item,
            post: {
              ...item.post,
              likeCount,
              commentCount,
              repostCount,
            },
          }
        : item,
    );
  }

  return {
    items,
    nextCursor,
    hasMore,
    initialized,
    ensureInitialized,
    refresh,
    loadMore,
    publishPost,
    toggleLike,
    repost,
  };
});
