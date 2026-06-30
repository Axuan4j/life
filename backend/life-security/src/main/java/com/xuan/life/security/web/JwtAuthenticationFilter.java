package com.xuan.life.security.web;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.service.LifeAuthenticatedUserLookupService;
import com.xuan.life.security.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final LifeAuthenticatedUserLookupService authenticatedUserLookupService;
    private final LifeAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
        JwtTokenService jwtTokenService,
        LifeAuthenticatedUserLookupService authenticatedUserLookupService,
        LifeAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtTokenService = jwtTokenService;
        this.authenticatedUserLookupService = authenticatedUserLookupService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);
        if (!StringUtils.hasText(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtTokenService.parseAccessToken(accessToken);
                LifeAuthenticatedUser tokenUser = jwtTokenService.toAuthenticatedUser(claims);
                LifeAuthenticatedUser latestUser = authenticatedUserLookupService.loadByRoleAndUserId(
                    tokenUser.getRole(),
                    tokenUser.getUserId()
                );

                // 这里显式按平台角色回查一次真实账号，避免拆表后仍然只按用户名回单表查询，造成身份串域。
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(latestUser, null, latestUser.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (BusinessException exception) {
                // JWT 解析失败要在安全层直接按 401 收口，避免冒泡到全局异常处理后被误报成 500。
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(
                    request,
                    response,
                    new JwtAuthenticationException(exception.getMessage(), exception)
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        // EventSource 原生不支持自定义 Authorization 头。
        // 这里仅对消息中心 SSE 放开 query token 兜底，避免把整个站点都退回到 query 鉴权模式。
        if ("/api/notifications/stream".equals(request.getRequestURI())) {
            return request.getParameter("accessToken");
        }
        return null;
    }
}
