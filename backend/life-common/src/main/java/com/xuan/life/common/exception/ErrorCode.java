package com.xuan.life.common.exception;

public enum ErrorCode {
    BAD_REQUEST(4000),
    UNAUTHORIZED(4010),
    FORBIDDEN(4030),
    NOT_FOUND(4040),
    CONFLICT(4090),
    SYSTEM_ERROR(5000);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
