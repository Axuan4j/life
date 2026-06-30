package com.xuan.life.security.service;

import java.util.Collection;

public interface AdditionalAuthorityProvider {

    Collection<String> loadAuthorities(Long userId, String username, String roleCode);
}
