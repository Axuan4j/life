package com.xuan.life.feed.model;

import com.xuan.life.common.exception.BusinessException;
import com.xuan.life.common.exception.ErrorCode;

public enum DiscoverResultType {
    TOPIC,
    KEYWORD;

    public static DiscoverResultType from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "discover type 不能为空");
        }
        try {
            return DiscoverResultType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的 discover type");
        }
    }
}
