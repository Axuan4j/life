import type { RouteRecordRaw } from 'vue-router';

// 这里只允许后端返回预先注册过的 viewKey，避免把数据库字段直接当成 import 路径执行。
export const viewRegistry: Record<string, () => Promise<RouteRecordRaw['component']>> = {
  OverviewView: async () => (await import('../views/overview/OverviewView.vue')).default,
  UserListView: async () => (await import('../views/user/UserListView.vue')).default,
  ContentGovernanceView: async () => (await import('../views/content/ContentGovernanceView.vue')).default,
  OperationCenterView: async () => (await import('../views/operation/OperationCenterView.vue')).default,
  AnalyticsView: async () => (await import('../views/analytics/AnalyticsView.vue')).default,
  SystemManageView: async () => (await import('../views/system/SystemManageView.vue')).default,
  PostListView: async () => (await import('../views/post/PostListView.vue')).default,
  BroadcastManageView: async () => (await import('../views/message/BroadcastManageView.vue')).default,
  MenuManageView: async () => (await import('../views/rbac/MenuManageView.vue')).default,
  RoleManageView: async () => (await import('../views/rbac/RoleManageView.vue')).default,
  AdminUserRoleView: async () => (await import('../views/rbac/AdminUserRoleView.vue')).default,
};
