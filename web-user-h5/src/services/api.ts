import { http } from './http';

export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  requestId: string;
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

export interface CaptchaClickPoint {
  x: number;
  y: number;
}

export interface LoginCaptchaChallenge {
  captchaId: string;
  imageData: string;
  targets: string[];
  width: number;
  height: number;
  expiresInSeconds: number;
}

export interface LoginCaptchaVerifyResult {
  tempKey: string;
  expiresInSeconds: number;
}

export interface CursorPageResponse<T> {
  items: T[];
  nextCursor: string;
  hasMore: boolean;
}

export interface PageResponse<T> {
  items: T[];
  total: number;
  pageNo: number;
  pageSize: number;
}

export type DiscoverResultType = 'TOPIC' | 'KEYWORD';
export type DiscoverResultSort = 'COMPOSITE' | 'LATEST';
export type EntityId = string;

export interface PostMediaResponse {
  mediaType: string;
  mediaUrl: string;
  sortOrder: number;
}

export interface PostCardResponse {
  postId: EntityId;
  authorId: EntityId;
  authorUsername: string;
  authorNickname: string;
  authorAvatarUrl: string | null;
  ipRegion: string;
  visibility?: 'PUBLIC' | 'PRIVATE' | string;
  contentText: string;
  publishedAt: string;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  medias: PostMediaResponse[];
}

export interface PostCommentReplyResponse {
  commentId: EntityId;
  userId: EntityId;
  username: string;
  nickname: string;
  avatarUrl: string | null;
  ipRegion: string;
  replyToUserId: EntityId | null;
  replyToUsername: string;
  replyToNickname: string;
  contentText: string;
  createdAt: string;
}

export interface PostCommentResponse {
  commentId: EntityId;
  userId: EntityId;
  username: string;
  nickname: string;
  avatarUrl: string | null;
  ipRegion: string;
  contentText: string;
  createdAt: string;
  replies: PostCommentReplyResponse[];
}

export interface PostLikedUserResponse {
  userId: EntityId;
  username: string;
  nickname: string;
  avatarUrl: string | null;
}

export interface PostInteractionResponse {
  likeCount: number;
  commentCount: number;
  repostCount: number;
  likedByCurrentUser: boolean;
  repostedByCurrentUser: boolean;
  likedUsers: PostLikedUserResponse[];
}

export interface PostRepostItemResponse {
  repostId: EntityId;
  userId: EntityId;
  username: string;
  nickname: string;
  avatarUrl: string | null;
  ipRegion: string;
  contentText: string;
  createdAt: string;
}

export interface PostDetailResponse {
  post: PostCardResponse;
  interaction: PostInteractionResponse;
}

export interface FeedItemResponse {
  sourceType: 'FOLLOWING' | 'RECOMMENDED';
  post: PostCardResponse;
}

export interface UserProfileResponse {
  userId: EntityId;
  username: string;
  nickname: string;
  avatarUrl: string | null;
  bio: string | null;
  lastLoginRegion: string | null;
  postCount: number;
  followingCount: number;
  followerCount: number;
  likedCount: number;
}

export interface FollowStatusResponse {
  targetUserId: EntityId;
  following: boolean;
}

export interface DiscoverHotKeywordItemResponse {
  rank: number;
  keyword: string;
  title: string;
  trendLabel: string;
  heatLabel: string;
}

export interface DiscoverTopicSquareItemResponse {
  topicKey: string;
  title: string;
  summary: string;
  discussionCount: number;
  coverStyle: string;
}

export interface DiscoverRecommendedAuthorItemResponse {
  userId: EntityId;
  username: string;
  nickname: string;
  avatarUrl: string | null;
  bio: string | null;
  followerCount: number;
  following: boolean;
  reason: string;
}

export interface DiscoverHomeResponse {
  hotKeywords: DiscoverHotKeywordItemResponse[];
  topicSquare: DiscoverTopicSquareItemResponse[];
  recommendedAuthors: DiscoverRecommendedAuthorItemResponse[];
}

export interface DiscoverResultHeaderResponse {
  resultType: DiscoverResultType;
  queryValue: string;
  title: string;
  subtitle: string;
  totalCount: number;
}

export interface DiscoverResultPageResponse {
  header: DiscoverResultHeaderResponse;
  items: FeedItemResponse[];
  nextCursor: string;
  hasMore: boolean;
}

export interface UserNotificationResponse {
  notificationId: EntityId;
  notificationType: 'LIKE' | 'COMMENT' | 'REPOST' | 'BROADCAST';
  senderUserId: EntityId | null;
  senderName: string;
  senderAvatarUrl: string | null;
  title: string;
  contentText: string;
  read: boolean;
  createdAt: string;
  postId: EntityId | null;
  commentId: EntityId | null;
}

export interface UnreadCountResponse {
  unreadCount: number;
}

export interface NotificationStreamEventResponse {
  eventType: 'SNAPSHOT' | 'NOTIFICATION_CREATED';
  unreadCount: number;
  notification: UserNotificationResponse | null;
}

export interface CreatePostRequest {
  contentText: string;
  visibility?: 'PUBLIC' | 'PRIVATE';
  medias: Array<{
    mediaType: string;
    mediaUrl: string;
    sortOrder: number;
  }>;
}

function unwrapResponse<T>(response: { data: ApiResponse<T> }) {
  return response.data.data;
}

export const authApi = {
  async fetchLoginCaptcha() {
    return unwrapResponse(await http.get<ApiResponse<LoginCaptchaChallenge>>('/api/auth/captcha'));
  },
  async verifyLoginCaptcha(payload: { captchaId: string; captchaPoints: CaptchaClickPoint[] }) {
    return unwrapResponse(await http.post<ApiResponse<LoginCaptchaVerifyResult>>('/api/auth/captcha/verify', payload));
  },
  async login(payload: {
    username: string;
    password: string;
    tempKey: string;
  }) {
    return unwrapResponse(await http.post<ApiResponse<TokenPair>>('/api/auth/login', payload));
  },
  async register(payload: { username: string; password: string; nickname?: string }) {
    return unwrapResponse(await http.post<ApiResponse<TokenPair>>('/api/auth/register', payload));
  },
  async refresh(refreshToken: string) {
    return unwrapResponse(await http.post<ApiResponse<TokenPair>>('/api/auth/refresh', { refreshToken }));
  },
  async logout() {
    return unwrapResponse(await http.post<ApiResponse<boolean>>('/api/auth/logout'));
  },
};

export const userApi = {
  async getMe() {
    return unwrapResponse(await http.get<ApiResponse<UserProfileResponse>>('/api/users/me'));
  },
  async getProfile(userId: EntityId) {
    return unwrapResponse(await http.get<ApiResponse<UserProfileResponse>>(`/api/users/${userId}/profile`));
  },
};

export const postApi = {
  async create(payload: CreatePostRequest) {
    return unwrapResponse(await http.post<ApiResponse<PostCardResponse>>('/api/posts', payload));
  },
  async getDetail(postId: EntityId) {
    return unwrapResponse(await http.get<ApiResponse<PostDetailResponse>>(`/api/posts/${postId}`));
  },
  async getComments(postId: EntityId) {
    return unwrapResponse(await http.get<ApiResponse<PostCommentResponse[]>>(`/api/posts/${postId}/comments`));
  },
  async getReposts(postId: EntityId) {
    return unwrapResponse(await http.get<ApiResponse<PostRepostItemResponse[]>>(`/api/posts/${postId}/reposts`));
  },
  async createComment(
    postId: EntityId,
    payload: { contentText: string; parentCommentId?: EntityId | null; replyToUserId?: EntityId | null },
  ) {
    return unwrapResponse(await http.post<ApiResponse<null>>(`/api/posts/${postId}/comments`, payload));
  },
  async deleteComment(postId: EntityId, commentId: EntityId) {
    return unwrapResponse(await http.delete<ApiResponse<null>>(`/api/posts/${postId}/comments/${commentId}`));
  },
  async toggleLike(postId: EntityId) {
    return unwrapResponse(await http.post<ApiResponse<PostInteractionResponse>>(`/api/posts/${postId}/likes/toggle`));
  },
  async repost(postId: EntityId) {
    return unwrapResponse(await http.post<ApiResponse<PostInteractionResponse>>(`/api/posts/${postId}/reposts`));
  },
  async listByUser(userId: EntityId, pageNo = 1, pageSize = 20) {
    return unwrapResponse(
      await http.get<ApiResponse<PostCardResponse[]>>(`/api/posts/users/${userId}`, {
        params: { pageNo, pageSize },
      }),
    );
  },
};

export const followApi = {
  async follow(targetUserId: EntityId) {
    return unwrapResponse(await http.post<ApiResponse<null>>(`/api/follows/${targetUserId}`));
  },
  async unfollow(targetUserId: EntityId) {
    return unwrapResponse(await http.delete<ApiResponse<null>>(`/api/follows/${targetUserId}`));
  },
  async getStatus(targetUserId: EntityId) {
    return unwrapResponse(await http.get<ApiResponse<FollowStatusResponse>>(`/api/follows/status/${targetUserId}`));
  },
  async getMyFollowingProfiles() {
    return unwrapResponse(await http.get<ApiResponse<UserProfileResponse[]>>('/api/follows/me/following/profiles'));
  },
};

export const feedApi = {
  async getHomeFeed(cursor = '', size = 10) {
    return unwrapResponse(
      await http.get<ApiResponse<CursorPageResponse<FeedItemResponse>>>('/api/feed/home', {
        params: { cursor: cursor || undefined, size },
      }),
    );
  },
};

export const discoverApi = {
  async getHome() {
    return unwrapResponse(await http.get<ApiResponse<DiscoverHomeResponse>>('/api/discover/home'));
  },
  async getResults(params: {
    type: DiscoverResultType;
    topicKey?: string;
    keyword?: string;
    sort?: DiscoverResultSort;
    cursor?: string;
    size?: number;
  }) {
    return unwrapResponse(
      await http.get<ApiResponse<DiscoverResultPageResponse>>('/api/discover/results', {
        params: {
          type: params.type,
          topicKey: params.topicKey,
          keyword: params.keyword,
          sort: params.sort ?? 'COMPOSITE',
          cursor: params.cursor || undefined,
          size: params.size ?? 10,
        },
      }),
    );
  },
};

export const notificationApi = {
  async list(pageNo = 1, pageSize = 20) {
    return unwrapResponse(
      await http.get<ApiResponse<PageResponse<UserNotificationResponse>>>('/api/notifications', {
        params: { pageNo, pageSize },
      }),
    );
  },
  async getUnreadCount() {
    return unwrapResponse(await http.get<ApiResponse<UnreadCountResponse>>('/api/notifications/unread-count'));
  },
  async markRead(notificationId: EntityId) {
    return unwrapResponse(await http.patch<ApiResponse<null>>(`/api/notifications/${notificationId}/read`));
  },
  async markAllRead() {
    return unwrapResponse(await http.patch<ApiResponse<null>>('/api/notifications/read-all'));
  },
};
