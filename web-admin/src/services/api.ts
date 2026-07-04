import { http } from './http';
import type {
  AdminAdminUserRoleItem,
  AdminAnalyticsOverview,
  AdminCommentListItem,
  AdminConfigListItem,
  AdminLoginLogItem,
  AdminLoginResponse,
  AdminMenuNode,
  AdminOperationLogItem,
  AdminOverview,
  AdminPostListItem,
  AdminReportListItem,
  AdminRoleDetail,
  AdminRoleOption,
  AdminSensitiveWordHitListItem,
  AdminSensitiveWordImportResult,
  AdminSensitiveWordListItem,
  AdminSessionResponse,
  AdminUserDetail,
  AdminUserListItem,
  MenuFormModel,
  RoleFormModel,
} from '../types/admin';
import type { ApiResponse, PageResponse } from '../types/http';

async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise;
  return response.data.data;
}

export function login(payload: { username: string; password: string }) {
  return unwrap<AdminLoginResponse>(http.post('/api/admin/auth/login', payload));
}

export function fetchSession() {
  return unwrap<AdminSessionResponse>(http.get('/api/admin/session'));
}

export function logout() {
  return unwrap<void>(http.post('/api/admin/auth/logout'));
}

export function fetchOverview() {
  return unwrap<AdminOverview>(http.get('/api/admin/overview'));
}

export function fetchUsers(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  roleCode?: string;
  status?: number | '';
}) {
  return unwrap<PageResponse<AdminUserListItem>>(http.get('/api/admin/users', { params }));
}

export function fetchUserDetail(userId: number) {
  return unwrap<AdminUserDetail>(http.get(`/api/admin/users/${userId}`));
}

export function updateUserGovernance(userId: number, payload: {
  accountStatus?: number;
  whitelistFlag?: number;
  postDisabled?: number;
  commentDisabled?: number;
  muteUntil?: string | null;
  clearMute?: number;
  forceLogout?: number;
  pointDelta?: number;
  reasonText?: string;
}) {
  return unwrap<void>(http.patch(`/api/admin/users/${userId}/governance`, payload));
}

export function fetchPosts(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  status?: number;
  reviewStatus?: number;
  visibility?: string;
}) {
  return unwrap<PageResponse<AdminPostListItem>>(http.get('/api/admin/posts', { params }));
}

export function updatePost(postId: number, payload: {
  status: number;
  reviewStatus: number;
  reviewReason: string;
  visibility: string;
}) {
  return unwrap<void>(http.patch(`/api/admin/posts/${postId}`, payload));
}

export function fetchContentComments(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  status?: number;
  postId?: number;
}) {
  return unwrap<PageResponse<AdminCommentListItem>>(http.get('/api/admin/content/comments', { params }));
}

export function updateContentComment(commentId: number, payload: { status: number }) {
  return unwrap<void>(http.patch(`/api/admin/content/comments/${commentId}`, payload));
}

export function fetchContentReports(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  targetType?: string;
  status?: number;
}) {
  return unwrap<PageResponse<AdminReportListItem>>(http.get('/api/admin/content/reports', { params }));
}

export function handleContentReport(payload: {
  targetType: string;
  targetId: number;
  status: number;
  handleAction: string;
  handleRemark: string;
}) {
  return unwrap<void>(http.patch('/api/admin/content/reports/handle', payload));
}

export function fetchSensitiveWords(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  actionType?: number;
  status?: number;
}) {
  return unwrap<PageResponse<AdminSensitiveWordListItem>>(http.get('/api/admin/content/sensitive-words', { params }));
}

export function createSensitiveWord(payload: {
  wordText: string;
  actionType: number;
  status: number;
  remark: string;
}) {
  return unwrap<void>(http.post('/api/admin/content/sensitive-words', payload));
}

export function updateSensitiveWord(wordId: number, payload: {
  wordText: string;
  actionType: number;
  status: number;
  remark: string;
}) {
  return unwrap<void>(http.put(`/api/admin/content/sensitive-words/${wordId}`, payload));
}

export function updateSensitiveWordStatus(wordId: number, payload: { status: number }) {
  return unwrap<void>(http.patch(`/api/admin/content/sensitive-words/${wordId}/status`, payload));
}

export function importSensitiveWords(payload: FormData) {
  return unwrap<AdminSensitiveWordImportResult>(
    http.post('/api/admin/content/sensitive-words/import', payload, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  );
}

export async function exportSensitiveWords(params: {
  keyword?: string;
  actionType?: number;
  status?: number;
}) {
  const response = await http.get('/api/admin/content/sensitive-words/export', {
    params,
    responseType: 'blob',
  });
  return response.data as Blob;
}

export function fetchSensitiveWordHits(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  targetType?: string;
  actionResult?: string;
}) {
  return unwrap<PageResponse<AdminSensitiveWordHitListItem>>(http.get('/api/admin/content/sensitive-word-hits', { params }));
}

export function sendBroadcast(payload: { title: string; contentText: string }) {
  return unwrap<void>(http.post('/api/admin/messages/broadcast', payload));
}

export function fetchDiscoverConfigs(configType?: string) {
  return unwrap<AdminConfigListItem[]>(http.get('/api/admin/operations/discover-configs', { params: { configType } }));
}

export function updateDiscoverConfig(configId: number, payload: {
  itemKey: string;
  title: string;
  subtitle: string;
  description: string;
  imageUrl: string;
  linkUrl: string;
  extraJson: string;
  sortOrder: number;
  status: number;
  remark?: string;
}) {
  return unwrap<void>(http.patch(`/api/admin/operations/discover-configs/${configId}`, payload));
}

export function fetchOperationConfigs(configType?: string) {
  return unwrap<AdminConfigListItem[]>(http.get('/api/admin/operations/admin-configs', { params: { configType } }));
}

export function updateOperationConfig(configId: number, payload: {
  itemKey: string;
  title: string;
  subtitle: string;
  description: string;
  imageUrl: string;
  linkUrl: string;
  extraJson: string;
  sortOrder: number;
  status: number;
  remark?: string;
}) {
  return unwrap<void>(http.patch(`/api/admin/operations/admin-configs/${configId}`, payload));
}

export function fetchAnalyticsOverview() {
  return unwrap<AdminAnalyticsOverview>(http.get('/api/admin/analytics/overview'));
}

export function fetchSystemConfigs() {
  return unwrap<AdminConfigListItem[]>(http.get('/api/admin/system/configs'));
}

export function updateSystemConfig(configId: number, payload: {
  itemKey: string;
  title: string;
  subtitle: string;
  description: string;
  imageUrl: string;
  linkUrl: string;
  extraJson: string;
  sortOrder: number;
  status: number;
  remark?: string;
}) {
  return unwrap<void>(http.patch(`/api/admin/system/configs/${configId}`, payload));
}

export function fetchOperationLogs(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  targetType?: string;
}) {
  return unwrap<PageResponse<AdminOperationLogItem>>(http.get('/api/admin/system/operation-logs', { params }));
}

export function fetchLoginLogs(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  resultStatus?: number;
}) {
  return unwrap<PageResponse<AdminLoginLogItem>>(http.get('/api/admin/system/login-logs', { params }));
}

export function fetchMenuTree() {
  return unwrap<AdminMenuNode[]>(http.get('/api/admin/rbac/menus/tree'));
}

export function createMenu(payload: MenuFormModel) {
  return unwrap<void>(http.post('/api/admin/rbac/menus', payload));
}

export function updateMenu(menuId: number, payload: MenuFormModel) {
  return unwrap<void>(http.put(`/api/admin/rbac/menus/${menuId}`, payload));
}

export function deleteMenu(menuId: number) {
  return unwrap<void>(http.delete(`/api/admin/rbac/menus/${menuId}`));
}

export function fetchRoles() {
  return unwrap<AdminRoleDetail[]>(http.get('/api/admin/rbac/roles'));
}

export function fetchRoleOptions() {
  return unwrap<AdminRoleOption[]>(http.get('/api/admin/rbac/roles/options'));
}

export function createRole(payload: RoleFormModel) {
  return unwrap<void>(http.post('/api/admin/rbac/roles', payload));
}

export function updateRole(roleId: number, payload: RoleFormModel) {
  return unwrap<void>(http.put(`/api/admin/rbac/roles/${roleId}`, payload));
}

export function deleteRole(roleId: number) {
  return unwrap<void>(http.delete(`/api/admin/rbac/roles/${roleId}`));
}

export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return unwrap<void>(http.patch(`/api/admin/rbac/roles/${roleId}/menus`, { menuIds }));
}

export function fetchAdminUsers(params: { pageNo: number; pageSize: number; keyword?: string }) {
  return unwrap<PageResponse<AdminAdminUserRoleItem>>(http.get('/api/admin/rbac/admin-users', { params }));
}

export function assignAdminUserRoles(userId: number, roleIds: number[]) {
  return unwrap<void>(http.patch(`/api/admin/rbac/admin-users/${userId}/roles`, { roleIds }));
}
