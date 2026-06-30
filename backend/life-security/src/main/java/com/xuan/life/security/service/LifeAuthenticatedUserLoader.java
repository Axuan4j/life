package com.xuan.life.security.service;

import com.xuan.life.security.model.LifeAuthenticatedUser;
import com.xuan.life.security.model.LifeRole;

public interface LifeAuthenticatedUserLoader {

    boolean supports(LifeRole role);

    LifeAuthenticatedUser loadByUserId(Long userId);

    LifeAuthenticatedUser loadByUsername(String username);
}
