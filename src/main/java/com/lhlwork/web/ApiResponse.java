package com.lhlwork.web;

import java.io.Serial;
import java.io.Serializable;


public record ApiResponse(String code, String message, Object data, Boolean success) implements Serializable {

    @Serial
    private static final long serialVersionUID = 189123782L;


    public static ApiResponse success(String message, Object data) {
        return new ApiResponse("1", message, data, true);
    }

    public static ApiResponse success(String message) {
        return new ApiResponse("1", message, null, true);
    }

    public static ApiResponse fail(String message) {
        return new ApiResponse("0", message, null, false);
    }

    public static ApiResponse fail(String message, Object data) {
        return new ApiResponse("0", message, data, false);
    }

    public static ApiResponse error() {
        return new ApiResponse("-1", "服务器忙，请稍后重试!", null, false);
    }
}
