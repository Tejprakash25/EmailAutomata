package com.emailautomata.core.web;

import org.springframework.http.ResponseEntity;

import java.net.URI;

/**
 * Factory helpers for the standard response shapes.
 *
 * <p>Keeps controllers to a single expressive line and guarantees that a
 * creation always carries a {@code Location} header — the kind of detail that
 * silently diverges when every controller builds its own ResponseEntity.</p>
 */
public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, URI location) {
        return ResponseEntity.created(location).body(ApiResponse.ok(data));
    }

    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
}