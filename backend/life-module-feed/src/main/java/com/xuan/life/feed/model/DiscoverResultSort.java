package com.xuan.life.feed.model;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;

public enum DiscoverResultSort {
    COMPOSITE,
    LATEST;

    public static DiscoverResultSort from(String value) {
        if (value == null || value.isBlank()) {
            return COMPOSITE;
        }
        try {
            return DiscoverResultSort.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的 discover sort");
        }
    }
}
