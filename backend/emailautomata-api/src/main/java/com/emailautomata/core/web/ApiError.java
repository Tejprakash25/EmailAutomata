package com.emailautomata.core.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Machine-readable failure detail.
 *
 * @param code    stable, screaming-snake identifier the client can branch on
 * @param message human-readable explanation, safe to surface in the UI
 * @param details optional field-level context (populated by validation handling)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        Map<String, String> details
) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }
}
