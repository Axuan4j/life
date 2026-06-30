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
  status: string;
  visibility: string;
  publishedAt: string;
  likeCount: number;
  commentCount: number;
  repostCount: number;
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
