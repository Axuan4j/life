package com.xuan.life.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.model.LifeRole;
import com.xuan.life.security.service.AdditionalAuthorityProvider;
import com.xuan.life.security.service.LifeAuthenticatedUserLoader;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.mapper.UserAccountMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserSecurityDetailsService implements UserDetailsService, LifeAuthenticatedUserLoader {

    private final UserAccountMapper userAccountMapper;
    private final ObjectProvider<List<AdditionalAuthorityProvider>> authorityProviders;

    public UserSecurityDetailsService(
        UserAccountMapper userAccountMapper,
        ObjectProvider<List<AdditionalAuthorityProvider>> authorityProviders
    ) {
        this.userAccountMapper = userAccountMapper;
        this.authorityProviders = authorityProviders;
    }

    @Override
    public LifeAuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadByUsername(username);
    }

    @Override
    public LifeAuthenticatedUser loadByUsername(String username) {
        UserAccount account = userAccountMapper.selectOne(
            new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUsername, username)
        );
        return buildAuthenticatedUser(account);
    }

    @Override
    public boolean supports(LifeRole role) {
        return role == LifeRole.USER;
    }

    @Override
    public LifeAuthenticatedUser loadByUserId(Long userId) {
        UserAccount account = userAccountMapper.selectById(userId);
        return buildAuthenticatedUser(account);
    }

    private LifeAuthenticatedUser buildAuthenticatedUser(UserAccount account) {
        if (account == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (account.getStatus() == null || account.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }

        // 这里把平台角色和后台动态权限一起装配进用户上下文，保证后台角色表一旦变更，下次请求即可实时生效。
        List<String> extraAuthorities = new ArrayList<>();
        authorityProviders.ifAvailable(providers -> providers.forEach(provider ->
            extraAuthorities.addAll(provider.loadAuthorities(account.getId(), account.getUsername(), account.getRoleCode()))
        ));

        return new LifeAuthenticatedUser(
            account.getId(),
            account.getUsername(),
            account.getPasswordHash(),
            LifeRole.fromCode(account.getRoleCode()),
            account.getStatus() != null && account.getStatus() == 1,
            extraAuthorities
        );
    }
}
