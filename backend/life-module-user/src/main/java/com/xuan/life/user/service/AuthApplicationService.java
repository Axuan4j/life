package com.xuan.life.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;
import com.xuan.life.infra.ip.IpRegionService;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.model.TokenPair;
import com.xuan.life.security.service.JwtTokenService;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.entity.UserProfile;
import com.xuan.life.user.mapper.UserAccountMapper;
import com.xuan.life.user.mapper.UserProfileMapper;
import com.xuan.life.user.web.request.LoginRequest;
import com.xuan.life.user.web.request.RefreshTokenRequest;
import com.xuan.life.user.web.request.RegisterRequest;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AuthApplicationService {

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserSecurityDetailsService userSecurityDetailsService;
    private final IpRegionService ipRegionService;

    public AuthApplicationService(
        UserAccountMapper userAccountMapper,
        UserProfileMapper userProfileMapper,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtTokenService jwtTokenService,
        UserSecurityDetailsService userSecurityDetailsService,
        IpRegionService ipRegionService
    ) {
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userSecurityDetailsService = userSecurityDetailsService;
        this.ipRegionService = ipRegionService;
    }

    @Transactional
    public TokenPair register(RegisterRequest request, String clientIp) {
        long existingCount = userAccountMapper.selectCount(
            new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUsername, request.username())
        );
        if (existingCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        UserAccount account = new UserAccount();
        account.setUsername(request.username());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRoleCode("USER");
        account.setStatus(1);
        userAccountMapper.insert(account);

        UserProfile profile = new UserProfile();
        profile.setUserId(account.getId());
        profile.setNickname(StringUtils.hasText(request.nickname()) ? request.nickname() : request.username());
        profile.setAvatarUrl("");
        profile.setBio("");
        userProfileMapper.insert(profile);

        return login(new LoginRequest(request.username(), request.password()), clientIp);
    }

    public TokenPair login(LoginRequest request, String clientIp) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        LifeAuthenticatedUser authenticatedUser = (LifeAuthenticatedUser) authentication.getPrincipal();
        updateLoginLocation(authenticatedUser.getUserId(), clientIp);
        return jwtTokenService.issueTokenPair(authenticatedUser);
    }

    public TokenPair refresh(RefreshTokenRequest request) {
        // refresh token 只负责续签会话，不改变当前用户角色与状态，真实身份仍以数据库实时加载结果为准。
        Claims claims = jwtTokenService.parseRefreshToken(request.refreshToken());
        LifeAuthenticatedUser user = userSecurityDetailsService.loadUserByUsername(claims.getSubject());
        return jwtTokenService.issueTokenPair(user);
    }

    public void logout() {
        // V1 仍保持 JWT 无状态退出：服务端不记黑名单，但接口固定保留，方便多端统一清理本地会话。
    }

    private void updateLoginLocation(Long userId, String clientIp) {
        UserAccount account = userAccountMapper.selectById(userId);
        if (account == null) {
            return;
        }
        account.setLastLoginIp(clientIp);
        account.setLastLoginRegion(ipRegionService.resolveRegion(clientIp));
        account.setLastLoginAt(LocalDateTime.now());
        userAccountMapper.updateById(account);
    }
}
