package com.gmail.organizer.dto;

import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        String requestUid,
        boolean success,
        T data,
        ApiError error) {

    public static <T> ApiResponse<T> success(String requestUid, T data) {
        return new ApiResponse<>(Instant.now(), requestUid, true, data, null);
    }

    public static ApiResponse<Void> failure(String requestUid, ApiError error) {
        return new ApiResponse<>(Instant.now(), requestUid, false, null, error);
    }
}
