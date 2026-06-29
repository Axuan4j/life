package com.xuan.life.security.web;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final LifeAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
        JwtTokenService jwtTokenService,
        UserDetailsService userDetailsService,
        LifeAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtTokenService.parseAccessToken(authorizationHeader.substring(7));
                LifeAuthenticatedUser tokenUser = jwtTokenService.toAuthenticatedUser(claims);
                LifeAuthenticatedUser latestUser =
                    (LifeAuthenticatedUser) userDetailsService.loadUserByUsername(tokenUser.getUsername());

                // 这里显式回查一次用户详情，避免 JWT 长时间有效时用户状态变化无法及时生效。
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
}
