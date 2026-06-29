package com.xuan.life.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.model.LifeRole;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.mapper.UserAccountMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserSecurityDetailsService implements UserDetailsService {

    private final UserAccountMapper userAccountMapper;

    public UserSecurityDetailsService(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public LifeAuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userAccountMapper.selectOne(
            new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUsername, username)
        );
        if (account == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (account.getStatus() == null || account.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
        return new LifeAuthenticatedUser(
            account.getId(),
            account.getUsername(),
            account.getPasswordHash(),
            LifeRole.fromCode(account.getRoleCode()),
            account.getStatus() != null && account.getStatus() == 1
        );
    }
}
