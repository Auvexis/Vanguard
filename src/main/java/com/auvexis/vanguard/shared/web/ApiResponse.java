package com.auvexis.vanguard.shared.web;

public record ApiResponse<T>(
        int status_code,
        String message,
        String error,
        T data) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, null, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, null, data);
    }

    public static <T> ApiResponse<T> no_content(String message) {
        return new ApiResponse<>(204, message, null, null);
    }

    public static <T> ApiResponse<T> error_generic(int status_code, String message, String error) {
        return new ApiResponse<>(status_code, message, error, null);
    }

    public static <T> ApiResponse<T> error_internal(String error) {
        return new ApiResponse<>(500, "Internal Server Error", error, null);
    }
}
