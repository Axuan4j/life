export interface AdminOperator {
  userId: number;
  username: string;
  nickname: string;
  avatarUrl: string;
  platformRoleCode: string;
  adminRoleCodes: string[];
  adminRoleNames: string[];
}

export interface AdminMenuNode {
  id: number;
  parentId: number;
  menuType: 'DIRECTORY' | 'PAGE';
  menuName: string;
  routeName: string;
  routePath: string;
  viewKey?: string | null;
  iconName?: string | null;
  permissionCode?: string | null;
  sortOrder: number;
  visible: number;
  status: number;
  isSystem: number;
  children: AdminMenuNode[];
}

export interface AdminSessionResponse {
  operator: AdminOperator;
  menus: AdminMenuNode[];
  permissions: string[];
  homePath: string;
}

export interface AdminLoginResponse extends AdminSessionResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface AdminOverview {
  userCount: number;
  postCount: number;
  todayPostCount: number;
  followCount: number;
}

export interface AdminUserListItem {
  userId: number;
  username: string;
  nickname: string;
  roleCode: string;
  status: number;
  createdAt: string;
  postCount: number;
  followingCount: number;
  followerCount: number;
}

export interface AdminPostListItem {
  postId: number;
  authorId: number;
  authorUsername: string;
  authorNickname: string;
  contentText: string;
  status: number;
  reviewStatus: number;
  reviewReason: string;
  visibility: string;
  publishedAt: string;
  likeCount: number;
  commentCount: number;
  repostCount: number;
}

export interface AdminCommentListItem {
  commentId: number;
  postId: number;
  postPreview: string;
  userId: number;
  username: string;
  nickname: string;
  replyToUserId: number | null;
  replyToNickname: string;
  contentText: string;
  status: number;
  createdAt: string;
}

export interface AdminReportListItem {
  targetType: string;
  targetId: number;
  targetLabel: string;
  targetPreview: string;
  reportCount: number;
  status: number;
  latestReasonCode: string;
  latestReasonText: string;
  handleAction: string;
  handleRemark: string;
  handlerName: string;
  handledAt: string | null;
  latestReportedAt: string;
}

export interface AdminSensitiveWordListItem {
  id: number;
  wordText: string;
  actionType: number;
  status: number;
  hitCount: number;
  lastHitAt: string | null;
  remark: string;
  updatedAt: string;
}

export interface AdminSensitiveWordHitListItem {
  id: number;
  targetType: string;
  targetId: number | null;
  userId: number | null;
  matchedWords: string[];
  actionResult: string;
  textExcerpt: string;
  createdAt: string;
}

export interface AdminSensitiveWordImportResult {
  totalLines: number;
  importedCount: number;
  duplicateCount: number;
  ignoredCount: number;
}

export interface AdminRoleOption {
  roleId: number;
  roleCode: string;
  roleName: string;
}

export interface AdminRoleDetail {
  roleId: number;
  roleCode: string;
  roleName: string;
  status: number;
  remark: string;
  isSystem: number;
  createdAt: string;
  menuIds: number[];
}

export interface AdminAdminUserRoleItem {
  userId: number;
  username: string;
  nickname: string;
  status: number;
  platformRoleCode: string;
  createdAt: string;
  roleIds: number[];
  roles: AdminRoleOption[];
}

export interface AdminUserCheckInLogItem {
  checkInDate: string;
  rewardPoints: number;
  createdAt: string;
}

export interface AdminUserModerationRecordItem {
  actionType: string;
  reasonText: string;
  operatorName: string;
  beforeSnapshot: string;
  afterSnapshot: string;
  createdAt: string;
}

export interface AdminUserDetail {
  userId: number;
  username: string;
  nickname: string;
  avatarUrl: string;
  bio: string;
  roleCode: string;
  accountStatus: number;
  lastLoginIp: string;
  lastLoginRegion: string;
  lastLoginAt: string | null;
  createdAt: string;
  postCount: number;
  publishedPostCount: number;
  commentCount: number;
  followingCount: number;
  followerCount: number;
  reportedCount: number;
  totalPoints: number;
  whitelistFlag: number;
  postDisabled: number;
  commentDisabled: number;
  muteUntil: string | null;
  lastForceLogoutAt: string | null;
  moderationNote: string;
  recentCheckIns: AdminUserCheckInLogItem[];
  moderationRecords: AdminUserModerationRecordItem[];
}

export interface AdminConfigListItem {
  id: number;
  configType: string;
  itemKey: string;
  title: string;
  subtitle: string;
  description: string;
  imageUrl: string;
  linkUrl: string;
  extraJson: string;
  sortOrder: number;
  status: number;
  remark: string;
  updatedAt: string;
}

export interface AdminOperationLogItem {
  id: number;
  operatorName: string;
  actionType: string;
  targetType: string;
  targetId: number | null;
  targetLabel: string;
  beforeSnapshot: string;
  afterSnapshot: string;
  remark: string;
  createdAt: string;
}

export interface AdminLoginLogItem {
  id: number;
  username: string;
  clientIp: string;
  ipRegion: string;
  resultStatus: number;
  failureReason: string;
  createdAt: string;
}

export interface AdminAnalyticsMetrics {
  userCount: number;
  postCount: number;
  commentCount: number;
  checkInCount: number;
}

export interface AdminAnalyticsTopPostItem {
  postId: number;
  authorNickname: string;
  contentText: string;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  status: number;
  reviewStatus: number;
}

export interface AdminAnalyticsRiskUserItem {
  userId: number;
  displayName: string;
  reportCount: number;
  postCount: number;
  commentCount: number;
}

export interface AdminAnalyticsOverview {
  totalMetrics: AdminAnalyticsMetrics;
  todayMetrics: AdminAnalyticsMetrics;
  yesterdayMetrics: AdminAnalyticsMetrics;
  activeAuthors7d: number;
  pendingReviewCount: number;
  pendingReportCount: number;
  topPosts: AdminAnalyticsTopPostItem[];
  riskUsers: AdminAnalyticsRiskUserItem[];
}

export interface MenuFormModel {
  parentId: number;
  menuType: 'DIRECTORY' | 'PAGE';
  menuName: string;
  routeName: string;
  routePath: string;
  viewKey: string;
  iconName: string;
  permissionCode: string;
  sortOrder: number;
  visible: number;
  status: number;
}

export interface RoleFormModel {
  roleCode: string;
  roleName: string;
  remark: string;
  status: number;
}
