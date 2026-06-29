import type {
  DiscoverHomeResponse,
  DiscoverRecommendedAuthorItemResponse,
  DiscoverResultHeaderResponse,
  DiscoverTopicSquareItemResponse,
  DiscoverHotKeywordItemResponse,
  FeedItemResponse,
  PostDetailResponse,
  PostCardResponse,
  PostCommentResponse,
  PostCommentReplyResponse,
  PostInteractionResponse,
  PostLikedUserResponse,
  PostMediaResponse,
  PostRepostItemResponse,
  UserProfileResponse,
} from './api';

export interface FeedImageCard {
  id: number;
  url: string;
  palette: string;
}

export interface FeedPostViewModel {
  postId: number;
  authorId: number;
  authorName: string;
  authorBadge: string;
  authorAvatarUrl: string;
  contentText: string;
  displayContentText: string;
  publishedAt: string;
  ipRegion: string;
  topic: string;
  location: string;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  images: FeedImageCard[];
}

export interface FeedItemViewModel {
  sourceType: 'FOLLOWING' | 'RECOMMENDED';
  post: FeedPostViewModel;
}

export interface CommentReplyViewModel {
  commentId: number;
  userId: number;
  displayName: string;
  username: string;
  avatarUrl: string;
  ipRegion: string;
  replyToDisplayName: string;
  contentText: string;
  createdAt: string;
}

export interface CommentViewModel {
  commentId: number;
  userId: number;
  displayName: string;
  username: string;
  avatarUrl: string;
  ipRegion: string;
  contentText: string;
  createdAt: string;
  replies: CommentReplyViewModel[];
}

export interface LikedUserViewModel {
  userId: number;
  displayName: string;
  username: string;
  avatarUrl: string;
}

export interface RepostItemViewModel {
  repostId: number;
  userId: number;
  displayName: string;
  username: string;
  avatarUrl: string;
  ipRegion: string;
  contentText: string;
  createdAt: string;
}

export interface PostInteractionViewModel {
  likeCount: number;
  commentCount: number;
  repostCount: number;
  likedByCurrentUser: boolean;
  repostedByCurrentUser: boolean;
  likedUsers: LikedUserViewModel[];
}

export interface DiscoverHotKeywordViewModel {
  rank: number;
  keyword: string;
  title: string;
  trendLabel: string;
  heatLabel: string;
}

export interface DiscoverTopicSquareViewModel {
  topicKey: string;
  title: string;
  summary: string;
  discussionCount: number;
  discussionText: string;
  coverStyle: string;
}

export interface DiscoverRecommendedAuthorViewModel {
  userId: number;
  username: string;
  displayName: string;
  avatarUrl: string;
  bio: string;
  followerCount: number;
  followerText: string;
  following: boolean;
  reason: string;
}

export interface DiscoverHomeViewModel {
  hotKeywords: DiscoverHotKeywordViewModel[];
  topicSquare: DiscoverTopicSquareViewModel[];
  recommendedAuthors: DiscoverRecommendedAuthorViewModel[];
}

export interface DiscoverResultHeaderViewModel {
  resultType: 'TOPIC' | 'KEYWORD';
  queryValue: string;
  title: string;
  subtitle: string;
  totalCount: number;
  totalCountText: string;
}

const imagePalettes = [
  'linear-gradient(135deg, #ffd3c3 0%, #ff8b68 100%)',
  'linear-gradient(135deg, #cfe4ff 0%, #7db0ff 100%)',
  'linear-gradient(135deg, #d9f6e2 0%, #7dcf95 100%)',
  'linear-gradient(135deg, #ffe4b8 0%, #ffb14a 100%)',
];

export function getFallbackAvatar(name: string) {
  const seed = name
    .split('')
    .reduce((total, current) => total + current.charCodeAt(0), 0);
  const palettes = [
    'linear-gradient(135deg, #ffd3c3 0%, #ff8b68 100%)',
    'linear-gradient(135deg, #cfe4ff 0%, #7db0ff 100%)',
    'linear-gradient(135deg, #f8d7ff 0%, #d895ff 100%)',
    'linear-gradient(135deg, #d9f6e2 0%, #7dcf95 100%)',
  ];
  return palettes[seed % palettes.length];
}

function mapMediaCards(medias: PostMediaResponse[]) {
  return (medias ?? []).map((media, index) => ({
    id: index + 1,
    url: media.mediaUrl,
    palette: imagePalettes[index % imagePalettes.length],
  }));
}

function extractExplicitTopic(contentText: string) {
  const matched = contentText.match(/^\s*#([^#\r\n]{1,20})#(?:\s+|$)/);
  return matched?.[1]?.trim() ?? '';
}

function buildDisplayContent(contentText: string, topic: string) {
  if (!topic) {
    return contentText;
  }
  // 只有用户明确在正文开头写了 #话题#，列表卡片才把它作为独立话题展示，
  // 避免过去那种“截正文前 14 个字冒充话题”的错误体验。
  return contentText.replace(/^\s*#[^#\r\n]{1,20}#(?:\s+)?/, '').trimStart();
}

export function formatRelativeTime(isoDateTime: string) {
  const target = new Date(isoDateTime);
  if (Number.isNaN(target.getTime())) {
    return '刚刚';
  }
  const diffMs = Date.now() - target.getTime();
  const diffMinutes = Math.max(1, Math.floor(diffMs / 60000));
  if (diffMinutes < 60) {
    return `${diffMinutes} 分钟前`;
  }
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) {
    return `${diffHours} 小时前`;
  }
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) {
    return `${diffDays} 天前`;
  }
  return `${target.getMonth() + 1}-${target.getDate()}`;
}

export function mapPostCardToFeedPost(post: PostCardResponse, sourceType: FeedItemResponse['sourceType']): FeedPostViewModel {
  const authorName = post.authorNickname || post.authorUsername || `用户 ${post.authorId}`;
  const topic = extractExplicitTopic(post.contentText);
  return {
    postId: post.postId,
    authorId: post.authorId,
    authorName,
    authorBadge: sourceType === 'FOLLOWING' ? '已关注' : '为你推荐',
    authorAvatarUrl: post.authorAvatarUrl ?? '',
    contentText: post.contentText,
    displayContentText: buildDisplayContent(post.contentText, topic),
    publishedAt: formatRelativeTime(post.publishedAt),
    ipRegion: post.ipRegion || '未知',
    topic,
    location: '公开可见',
    likeCount: post.likeCount,
    commentCount: post.commentCount,
    repostCount: post.repostCount,
    images: mapMediaCards(post.medias),
  };
}

export function mapFeedItem(item: FeedItemResponse): FeedItemViewModel {
  // 这里把后端的业务 DTO 映射为页面视图模型，避免视图层直接依赖接口原始字段，
  // 后续 Android / H5 调整表现时也能在这一层统一收口。
  return {
    sourceType: item.sourceType,
    post: mapPostCardToFeedPost(item.post, item.sourceType),
  };
}

export function mapUserProfile(profile: UserProfileResponse) {
  return {
    ...profile,
    displayName: profile.nickname || profile.username,
    bio: profile.bio || '在这里记录热爱，分享每一个值得被看见的瞬间。',
    avatarUrl: profile.avatarUrl ?? '',
    lastLoginRegion: profile.lastLoginRegion || '未知',
  };
}

function formatCountText(count: number) {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`;
  }
  return `${count}`;
}

function mapDiscoverHotKeyword(item: DiscoverHotKeywordItemResponse): DiscoverHotKeywordViewModel {
  return {
    rank: item.rank,
    keyword: item.keyword,
    title: item.title,
    trendLabel: item.trendLabel,
    heatLabel: item.heatLabel,
  };
}

function mapDiscoverTopicSquare(item: DiscoverTopicSquareItemResponse): DiscoverTopicSquareViewModel {
  return {
    topicKey: item.topicKey,
    title: item.title,
    summary: item.summary,
    discussionCount: item.discussionCount,
    discussionText: `${formatCountText(item.discussionCount)} 讨论`,
    coverStyle: item.coverStyle,
  };
}

function mapDiscoverRecommendedAuthor(item: DiscoverRecommendedAuthorItemResponse): DiscoverRecommendedAuthorViewModel {
  return {
    userId: item.userId,
    username: item.username,
    displayName: item.nickname || item.username,
    avatarUrl: item.avatarUrl ?? '',
    bio: item.bio ?? '分享热爱，记录值得被看见的日常。',
    followerCount: item.followerCount,
    followerText: `${formatCountText(item.followerCount)} 粉丝`,
    following: item.following,
    reason: item.reason,
  };
}

export function mapDiscoverHome(response: DiscoverHomeResponse): DiscoverHomeViewModel {
  return {
    hotKeywords: response.hotKeywords.map(mapDiscoverHotKeyword),
    topicSquare: response.topicSquare.map(mapDiscoverTopicSquare),
    recommendedAuthors: response.recommendedAuthors.map(mapDiscoverRecommendedAuthor),
  };
}

export function mapDiscoverResultHeader(header: DiscoverResultHeaderResponse): DiscoverResultHeaderViewModel {
  return {
    resultType: header.resultType,
    queryValue: header.queryValue,
    title: header.title,
    subtitle: header.subtitle,
    totalCount: header.totalCount,
    totalCountText: `${formatCountText(header.totalCount)} 条内容`,
  };
}

function mapCommentReply(reply: PostCommentReplyResponse): CommentReplyViewModel {
  return {
    commentId: reply.commentId,
    userId: reply.userId,
    displayName: reply.nickname || reply.username,
    username: reply.username,
    avatarUrl: reply.avatarUrl ?? '',
    ipRegion: reply.ipRegion || '未知',
    replyToDisplayName: reply.replyToNickname || reply.replyToUsername,
    contentText: reply.contentText,
    createdAt: formatRelativeTime(reply.createdAt),
  };
}

export function mapComment(comment: PostCommentResponse): CommentViewModel {
  return {
    commentId: comment.commentId,
    userId: comment.userId,
    displayName: comment.nickname || comment.username,
    username: comment.username,
    avatarUrl: comment.avatarUrl ?? '',
    ipRegion: comment.ipRegion || '未知',
    contentText: comment.contentText,
    createdAt: formatRelativeTime(comment.createdAt),
    replies: (comment.replies ?? []).map(mapCommentReply),
  };
}

function mapLikedUser(user: PostLikedUserResponse): LikedUserViewModel {
  return {
    userId: user.userId,
    displayName: user.nickname || user.username,
    username: user.username,
    avatarUrl: user.avatarUrl ?? '',
  };
}

export function mapRepostItem(item: PostRepostItemResponse): RepostItemViewModel {
  return {
    repostId: item.repostId,
    userId: item.userId,
    displayName: item.nickname || item.username,
    username: item.username,
    avatarUrl: item.avatarUrl ?? '',
    ipRegion: item.ipRegion || '未知',
    contentText: item.contentText,
    createdAt: formatRelativeTime(item.createdAt),
  };
}

export function mapPostInteraction(interaction: PostInteractionResponse): PostInteractionViewModel {
  return {
    likeCount: interaction.likeCount,
    commentCount: interaction.commentCount,
    repostCount: interaction.repostCount,
    likedByCurrentUser: interaction.likedByCurrentUser,
    repostedByCurrentUser: interaction.repostedByCurrentUser,
    likedUsers: (interaction.likedUsers ?? []).map(mapLikedUser),
  };
}

export function mapPostDetail(detail: PostDetailResponse) {
  return {
    post: mapPostCardToFeedPost(detail.post, 'FOLLOWING'),
    interaction: mapPostInteraction(detail.interaction),
  };
}
