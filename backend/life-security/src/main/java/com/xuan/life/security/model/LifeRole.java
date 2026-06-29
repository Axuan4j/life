package com.xuan.life.security.model;

import java.util.Arrays;

public enum LifeRole {
    USER,
    ADMIN;

    public static LifeRole fromCode(String code) {
        return Arrays.stream(values())
            .filter(role -> role.name().equalsIgnoreCase(code))
            .findFirst()
            .orElse(USER);
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
