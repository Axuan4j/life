package com.xuan.life.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.life.admin.entity.AdminAccount;
import com.xuan.life.admin.model.AdminLoginResultCodes;
import com.xuan.life.admin.mapper.AdminAccountMapper;
import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.infra.ip.IpRegionService;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.model.LifeRole;
import com.xuan.life.security.service.AdditionalAuthorityProvider;
import com.xuan.life.security.service.LifeAuthenticatedUserLoader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminSecurityDetailsService implements LifeAuthenticatedUserLoader {

    private final AdminAccountMapper adminAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final IpRegionService ipRegionService;
    private final ObjectProvider<List<AdditionalAuthorityProvider>> authorityProviders;
    private final AdminLoginLogService adminLoginLogService;

    public AdminSecurityDetailsService(
        AdminAccountMapper adminAccountMapper,
        PasswordEncoder passwordEncoder,
        IpRegionService ipRegionService,
        ObjectProvider<List<AdditionalAuthorityProvider>> authorityProviders,
        AdminLoginLogService adminLoginLogService
    ) {
        this.adminAccountMapper = adminAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.ipRegionService = ipRegionService;
        this.authorityProviders = authorityProviders;
        this.adminLoginLogService = adminLoginLogService;
    }

    @Override
    public boolean supports(LifeRole role) {
        return role == LifeRole.ADMIN;
    }

    @Override
    public LifeAuthenticatedUser loadByUserId(Long userId) {
        AdminAccount account = adminAccountMapper.selectById(userId);
        return buildAuthenticatedUser(account);
    }

    @Override
    public LifeAuthenticatedUser loadByUsername(String username) {
        AdminAccount account = adminAccountMapper.selectOne(
            new LambdaQueryWrapper<AdminAccount>().eq(AdminAccount::getUsername, username)
        );
        return buildAuthenticatedUser(account);
    }

    public LifeAuthenticatedUser authenticate(String username, String rawPassword, String clientIp) {
        String ipRegion = ipRegionService.resolveRegion(clientIp);
        AdminAccount account = adminAccountMapper.selectOne(
            new LambdaQueryWrapper<AdminAccount>().eq(AdminAccount::getUsername, username)
        );
        if (account == null || !passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            adminLoginLogService.log(null, username, clientIp, ipRegion, AdminLoginResultCodes.FAILED, "用户名或密码错误");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (account.getStatus() == null || account.getStatus() != 1) {
            adminLoginLogService.log(account.getId(), account.getUsername(), clientIp, ipRegion, AdminLoginResultCodes.FAILED, "管理员账号已被禁用");
            throw new BusinessException(ErrorCode.FORBIDDEN, "管理员账号已被禁用");
        }

        LifeAuthenticatedUser authenticatedUser = buildAuthenticatedUser(account);
        updateLoginLocation(account, clientIp, ipRegion);
        adminLoginLogService.log(account.getId(), account.getUsername(), clientIp, ipRegion, AdminLoginResultCodes.SUCCESS, "");
        return authenticatedUser;
    }

    public AdminAccount getAccountOrThrow(Long userId) {
        AdminAccount account = adminAccountMapper.selectById(userId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "管理员账号不存在");
        }
        return account;
    }

    private LifeAuthenticatedUser buildAuthenticatedUser(AdminAccount account) {
        if (account == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (account.getStatus() == null || account.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "管理员账号已被禁用");
        }

        // 后台账号也统一走动态权限装配，角色菜单一旦调整，下一次请求即可回查到最新页面权限。
        List<String> extraAuthorities = new ArrayList<>();
        authorityProviders.ifAvailable(providers -> providers.forEach(provider ->
            extraAuthorities.addAll(provider.loadAuthorities(account.getId(), account.getUsername(), LifeRole.ADMIN.name()))
        ));

        return new LifeAuthenticatedUser(
            account.getId(),
            account.getUsername(),
            account.getPasswordHash(),
            LifeRole.ADMIN,
            true,
            account.getTokenVersion() == null ? 0L : account.getTokenVersion(),
            extraAuthorities
        );
    }

    private void updateLoginLocation(AdminAccount account, String clientIp, String ipRegion) {
        account.setLastLoginIp(clientIp);
        account.setLastLoginRegion(ipRegion);
        account.setLastLoginAt(LocalDateTime.now());
        adminAccountMapper.updateById(account);
    }
}
