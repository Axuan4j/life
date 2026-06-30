package com.xuan.life.admin.web.response;

import java.time.LocalDateTime;
import java.util.List;

public record AdminRoleDetailResponse(
    Long roleId,
    String roleCode,
    String roleName,
    Integer status,
    String remark,
    Integer isSystem,
    LocalDateTime createdAt,
    List<Long> menuIds
) {
}
