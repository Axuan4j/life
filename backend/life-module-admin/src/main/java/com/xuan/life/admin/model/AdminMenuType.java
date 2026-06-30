package com.xuan.life.admin.model;

import java.util.Arrays;

public enum AdminMenuType {
    DIRECTORY,
    PAGE;

    public static AdminMenuType fromCode(String code) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(code))
            .findFirst()
            .orElse(PAGE);
    }
}
