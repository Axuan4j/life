export interface MockFeedImage {
  id: number;
  palette: string;
}

export interface MockFeedPost {
  postId: number;
  authorId: number;
  authorName: string;
  authorBadge: string;
  sourceType: 'FOLLOWING' | 'RECOMMENDED';
  topic: string;
  contentText: string;
  publishedAt: string;
  location: string;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  images: MockFeedImage[];
}

export interface MockProfile {
  userId: number;
  username: string;
  nickname: string;
  bio: string;
  level: string;
  followingCount: string;
  postCount: string;
  followerCount: string;
  likedCount: string;
}

const initialFeed: MockFeedPost[] = [
  {
    postId: 1001,
    authorId: 2001,
    authorName: '小满今天也很开心',
    authorBadge: '旅行达人',
    sourceType: 'FOLLOWING',
    topic: '旅行日记',
    contentText: '在洱海边吹了整整一个下午的风，湖水蓝蓝，风也温柔，生活本该慢一点。',
    publishedAt: '2 小时前',
    location: '大理 · 洱海',
    likeCount: 128,
    commentCount: 23,
    repostCount: 12,
    images: [
      { id: 1, palette: 'linear-gradient(135deg, #6bbcff 0%, #2d7ff9 100%)' },
      { id: 2, palette: 'linear-gradient(135deg, #9be4b4 0%, #5dbf85 100%)' },
      { id: 3, palette: 'linear-gradient(135deg, #8cd0ff 0%, #5ca9f4 100%)' },
    ],
  },
  {
    postId: 1002,
    authorId: 2002,
    authorName: '一颗柠檬',
    authorBadge: '美食爱好者',
    sourceType: 'RECOMMENDED',
    topic: '美食探店',
    contentText: '这家街角的小店藏不住了。咖喱鸡肉饭绝绝子，汤汁浓郁，米饭粒粒分明。',
    publishedAt: '5 小时前',
    location: '静安 · 老街角',
    likeCount: 96,
    commentCount: 18,
    repostCount: 6,
    images: [
      { id: 4, palette: 'linear-gradient(135deg, #f7b267 0%, #f4845f 100%)' },
      { id: 5, palette: 'linear-gradient(135deg, #ffd166 0%, #ef476f 100%)' },
      { id: 6, palette: 'linear-gradient(135deg, #f4a261 0%, #e76f51 100%)' },
    ],
  },
]

export const mockProfile: MockProfile = {
  userId: 10001,
  username: 'life_user',
  nickname: '微风与海',
  bio: '热爱生活，记录美好瞬间',
  level: 'Lv.6',
  followingCount: '356',
  postCount: '128',
  followerCount: '892',
  likedCount: '2.4k',
}

export function createInitialFeed(): MockFeedPost[] {
  return initialFeed.map((item) => ({
    ...item,
    images: item.images.map((image) => ({ ...image })),
  }))
}

export function createMockPost(contentText: string): MockFeedPost {
  return {
    postId: Date.now(),
    authorId: mockProfile.userId,
    authorName: mockProfile.nickname,
    authorBadge: '新的分享',
    sourceType: 'FOLLOWING',
    topic: '新的动态',
    contentText,
    publishedAt: '刚刚',
    location: '所在位置',
    likeCount: 0,
    commentCount: 0,
    repostCount: 0,
    images: [
      { id: Date.now() + 1, palette: 'linear-gradient(135deg, #8cd0ff 0%, #5ca9f4 100%)' },
    ],
  }
}
