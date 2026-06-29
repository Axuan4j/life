package com.xuan.life.common.api;

import com.xuan.life.common.web.RequestIdContext;

public final class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;
    private final String requestId;

    private ApiResponse(int code, String message, T data, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "OK", data, RequestIdContext.getRequestId());
    }

    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, message, null, RequestIdContext.getRequestId());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getRequestId() {
        return requestId;
    }
}
