package com.xuan.life.security.service;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.model.LifeRole;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LifeAuthenticatedUserLookupService {

    private final List<LifeAuthenticatedUserLoader> authenticatedUserLoaders;

    public LifeAuthenticatedUserLookupService(List<LifeAuthenticatedUserLoader> authenticatedUserLoaders) {
        this.authenticatedUserLoaders = authenticatedUserLoaders;
    }

    public LifeAuthenticatedUser loadByRoleAndUserId(LifeRole role, Long userId) {
        return resolveLoader(role).loadByUserId(userId);
    }

    public LifeAuthenticatedUser loadByRoleAndUsername(LifeRole role, String username) {
        return resolveLoader(role).loadByUsername(username);
    }

    private LifeAuthenticatedUserLoader resolveLoader(LifeRole role) {
        // token 里带上的平台角色决定去哪个账号域回查，避免拆表后仍按单表用户名误查到另一侧身份。
        return authenticatedUserLoaders.stream()
            .filter(loader -> loader.supports(role))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "无效的账号类型"));
    }
}
