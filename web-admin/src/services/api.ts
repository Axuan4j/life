import { http } from './http';
import type {
  AdminAdminUserRoleItem,
  AdminLoginResponse,
  AdminMenuNode,
  AdminOverview,
  AdminPostListItem,
  AdminRoleDetail,
  AdminRoleOption,
  AdminSessionResponse,
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
  return unwrap<void>(http.post('/api/auth/logout'));
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

export function fetchPosts(params: {
  pageNo: number;
  pageSize: number;
  keyword?: string;
  status?: string;
  visibility?: string;
}) {
  return unwrap<PageResponse<AdminPostListItem>>(http.get('/api/admin/posts', { params }));
}

export function updatePost(postId: number, payload: { status: string; visibility: string }) {
  return unwrap<void>(http.patch(`/api/admin/posts/${postId}`, payload));
}

export function sendBroadcast(payload: { title: string; contentText: string }) {
  return unwrap<void>(http.post('/api/admin/messages/broadcast', payload));
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
