const platformRoleLabelMap: Record<string, string> = {
  USER: '普通用户',
  ADMIN: '管理员',
};

const adminRoleCodeLabelMap: Record<string, string> = {
  SUPER_ADMIN: '超级管理员',
};

const postStatusLabelMap: Record<number, string> = {
  0: '已隐藏',
  1: '已发布',
};

const reviewStatusLabelMap: Record<number, string> = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝',
  3: '已下架',
};

const commentStatusLabelMap: Record<number, string> = {
  0: '已隐藏',
  1: '可见',
  2: '已删除',
};

const reportStatusLabelMap: Record<number, string> = {
  0: '待处理',
  1: '已忽略',
  2: '已处理',
  3: '恶意举报',
};

const loginResultLabelMap: Record<number, string> = {
  0: '失败',
  1: '成功',
};

const visibilityLabelMap: Record<string, string> = {
  PUBLIC: '公开可见',
  PRIVATE: '仅自己可见',
};

const menuTypeLabelMap: Record<string, string> = {
  DIRECTORY: '目录',
  PAGE: '页面',
};

const targetTypeLabelMap: Record<string, string> = {
  POST: '帖子',
  COMMENT: '评论',
  USER: '用户',
};

const sensitiveActionLabelMap: Record<number, string> = {
  0: '提示放行',
  1: '直接拒绝',
  2: '转人工审核',
};

const sensitiveHitActionResultLabelMap: Record<string, string> = {
  WARN_PASSED: '已放行提醒',
  REVIEW_PENDING: '已转审核',
  POST_REJECTED: '帖子已拦截',
  COMMENT_REJECTED: '评论已拦截',
};

const discoverConfigTypeLabelMap: Record<string, string> = {
  HOT_KEYWORD: '热搜词',
  TOPIC: '话题广场',
  RECOMMENDED_AUTHOR: '推荐作者',
  BANNER: '发现 Banner',
};

const adminConfigTypeLabelMap: Record<string, string> = {
  CONTENT_TAG: '内容标签',
  APP_NOTICE: '站内公告',
  APP_ACTIVITY: '活动配置',
  NOTIFICATION_TEMPLATE: '通知模板',
  SYSTEM_CONFIG: '系统参数',
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

export function formatPostStatusLabel(status?: number | null) {
  if (status === null || status === undefined) {
    return '-';
  }
  return postStatusLabelMap[status] ?? String(status);
}

export function formatReviewStatusLabel(status?: number | null) {
  if (status === null || status === undefined) {
    return '-';
  }
  return reviewStatusLabelMap[status] ?? String(status);
}

export function formatCommentStatusLabel(status?: number | null) {
  if (status === null || status === undefined) {
    return '-';
  }
  return commentStatusLabelMap[status] ?? String(status);
}

export function formatReportStatusLabel(status?: number | null) {
  if (status === null || status === undefined) {
    return '-';
  }
  return reportStatusLabelMap[status] ?? String(status);
}

export function formatLoginResultLabel(status?: number | null) {
  if (status === null || status === undefined) {
    return '-';
  }
  return loginResultLabelMap[status] ?? String(status);
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

export function formatTargetTypeLabel(targetType?: string | null) {
  if (!targetType) {
    return '-';
  }
  return targetTypeLabelMap[targetType] ?? targetType;
}

export function formatSensitiveActionLabel(actionType?: number | null) {
  if (actionType === null || actionType === undefined) {
    return '-';
  }
  return sensitiveActionLabelMap[actionType] ?? String(actionType);
}

export function formatSensitiveHitActionResultLabel(actionResult?: string | null) {
  if (!actionResult) {
    return '-';
  }
  return sensitiveHitActionResultLabelMap[actionResult] ?? actionResult;
}

export function formatDiscoverConfigTypeLabel(configType?: string | null) {
  if (!configType) {
    return '-';
  }
  return discoverConfigTypeLabelMap[configType] ?? configType;
}

export function formatAdminConfigTypeLabel(configType?: string | null) {
  if (!configType) {
    return '-';
  }
  return adminConfigTypeLabelMap[configType] ?? configType;
}
