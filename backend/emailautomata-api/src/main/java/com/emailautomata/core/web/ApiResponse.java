package com.emailautomata.core.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

/**
 * Uniform response envelope for every EmailAutomata endpoint.
 *
 * <p>A single, predictable shape means the React client has exactly one
 * unwrapping path and one error path — no per-endpoint special cases.</p>
 *
 * @param <T> payload type carried on success
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        OffsetDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(false, null, error, OffsetDateTime.now());
    }
}
