const platformRoleLabelMap: Record<string, string> = {
  USER: '普通用户',
  ADMIN: '管理员',
};

const adminRoleCodeLabelMap: Record<string, string> = {
  SUPER_ADMIN: '超级管理员',
};

const postStatusLabelMap: Record<string, string> = {
  PUBLISHED: '已发布',
  HIDDEN: '已隐藏',
};

const visibilityLabelMap: Record<string, string> = {
  PUBLIC: '公开可见',
  PRIVATE: '仅自己可见',
};

const menuTypeLabelMap: Record<string, string> = {
  DIRECTORY: '目录',
  PAGE: '页面',
};

export function formatPlatformRoleLabel(roleCode?: string | null) {
  if (!roleCode) {
    return '-';
  }
  return platformRoleLabelMap[roleCode] ?? roleCode;
}

export function formatAdminRoleCodeLabel(roleCode?: string | null) {
  if (!roleCode) {
    return '-';
  }
  return adminRoleCodeLabelMap[roleCode] ?? roleCode;
}

export function formatPostStatusLabel(status?: string | null) {
  if (!status) {
    return '-';
  }
  return postStatusLabelMap[status] ?? status;
}

export function formatVisibilityLabel(visibility?: string | null) {
  if (!visibility) {
    return '-';
  }
  return visibilityLabelMap[visibility] ?? visibility;
}

export function formatMenuTypeLabel(menuType?: string | null) {
  if (!menuType) {
    return '-';
  }
  return menuTypeLabelMap[menuType] ?? menuType;
}
