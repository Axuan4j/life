package com.xuan.life.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LifeAuthenticatedUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final LifeRole role;
    private final boolean enabled;
    private final List<String> extraAuthorities;

    public LifeAuthenticatedUser(Long userId, String username, String password, LifeRole role, boolean enabled) {
        this(userId, username, password, role, enabled, List.of());
    }

    public LifeAuthenticatedUser(
        Long userId,
        String username,
        String password,
        LifeRole role,
        boolean enabled,
        List<String> extraAuthorities
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.extraAuthorities = extraAuthorities == null ? List.of() : List.copyOf(extraAuthorities);
    }

    public Long getUserId() {
        return userId;
    }

    public LifeRole getRole() {
        return role;
    }

    public List<String> getExtraAuthorities() {
        return extraAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> authorities = new LinkedHashSet<>();
        authorities.add(role.authority());
        authorities.addAll(extraAuthorities);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(authorities.size());
        authorities.forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority)));
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
