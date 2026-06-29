import { ref } from 'vue';
import { defineStore } from 'pinia';
import {
  discoverApi,
  type DiscoverResultSort,
  type DiscoverResultType,
  type PostInteractionResponse,
} from '../services/api';
import {
  mapDiscoverHome,
  mapDiscoverResultHeader,
  mapFeedItem,
  type DiscoverHomeViewModel,
  type DiscoverResultHeaderViewModel,
  type FeedItemViewModel,
} from '../services/view-models';

const RECENT_SEARCHES_KEY = 'life_discover_recent_searches';

interface DiscoverQueryState {
  type: DiscoverResultType;
  topicKey?: string;
  keyword?: string;
}

function loadRecentSearches() {
  const raw = localStorage.getItem(RECENT_SEARCHES_KEY);
  if (!raw) {
    return [] as string[];
  }
  return raw
    .split('|')
    .map((item) => item.trim())
    .filter(Boolean)
    .slice(0, 8);
}

export const useDiscoverStore = defineStore('discover', () => {
  const home = ref<DiscoverHomeViewModel | null>(null);
  const homeLoaded = ref(false);
  const recentSearches = ref<string[]>(loadRecentSearches());

  const resultHeader = ref<DiscoverResultHeaderViewModel | null>(null);
  const resultItems = ref<FeedItemViewModel[]>([]);
  const resultNextCursor = ref('');
  const resultHasMore = ref(true);
  const resultSort = ref<DiscoverResultSort>('COMPOSITE');
  const currentQuery = ref<DiscoverQueryState | null>(null);

  async function loadHome(force = false) {
    if (homeLoaded.value && !force) {
      return;
    }
    const response = await discoverApi.getHome();
    home.value = mapDiscoverHome(response);
    homeLoaded.value = true;
  }

  async function refreshResults(query: DiscoverQueryState, sort: DiscoverResultSort = 'COMPOSITE') {
    currentQuery.value = query;
    resultSort.value = sort;
    const response = await discoverApi.getResults({
      ...query,
      sort,
      cursor: '',
      size: 10,
    });
    resultHeader.value = mapDiscoverResultHeader(response.header);
    resultItems.value = response.items.map(mapFeedItem);
    resultNextCursor.value = response.nextCursor;
    resultHasMore.value = response.hasMore;

    if (query.type === 'KEYWORD' && query.keyword) {
      rememberSearch(query.keyword);
    }
  }

  async function loadMoreResults() {
    if (!currentQuery.value || !resultHasMore.value) {
      return;
    }
    const response = await discoverApi.getResults({
      ...currentQuery.value,
      sort: resultSort.value,
      cursor: resultNextCursor.value,
      size: 10,
    });
    resultHeader.value = mapDiscoverResultHeader(response.header);
    resultItems.value = [...resultItems.value, ...response.items.map(mapFeedItem)];
    resultNextCursor.value = response.nextCursor;
    resultHasMore.value = response.hasMore;
  }

  function rememberSearch(keyword: string) {
    const normalized = keyword.trim();
    if (!normalized) {
      return;
    }
    recentSearches.value = [normalized, ...recentSearches.value.filter((item) => item !== normalized)].slice(0, 8);
    localStorage.setItem(RECENT_SEARCHES_KEY, recentSearches.value.join('|'));
  }

  function removeRecentSearch(keyword: string) {
    recentSearches.value = recentSearches.value.filter((item) => item !== keyword);
    localStorage.setItem(RECENT_SEARCHES_KEY, recentSearches.value.join('|'));
  }

  function clearRecentSearches() {
    recentSearches.value = [];
    localStorage.removeItem(RECENT_SEARCHES_KEY);
  }

  function updateRecommendedAuthorFollowState(userId: number, following: boolean) {
    if (!home.value) {
      return;
    }
    home.value = {
      ...home.value,
      recommendedAuthors: home.value.recommendedAuthors.map((author) =>
        author.userId === userId
          ? {
              ...author,
              following,
            }
          : author,
      ),
    };
  }

  function updateResultPostCounters(postId: number, interaction: PostInteractionResponse) {
    resultItems.value = resultItems.value.map((item) =>
      item.post.postId === postId
        ? {
            ...item,
            post: {
              ...item.post,
              likeCount: interaction.likeCount,
              commentCount: interaction.commentCount,
              repostCount: interaction.repostCount,
            },
          }
        : item,
    );
  }

  return {
    home,
    homeLoaded,
    recentSearches,
    resultHeader,
    resultItems,
    resultNextCursor,
    resultHasMore,
    resultSort,
    currentQuery,
    loadHome,
    refreshResults,
    loadMoreResults,
    rememberSearch,
    removeRecentSearch,
    clearRecentSearches,
    updateRecommendedAuthorFollowState,
    updateResultPostCounters,
  };
});
