package com.emailautomata.core.error;

import java.util.Map;

/**
 * Thrown when creating a resource would violate a uniqueness rule.
 *
 * <p>Carries the offending field in {@code details} so the client can attach
 * the message to the right input rather than showing a banner.</p>
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                "A %s with that %s already exists.".formatted(resource.toLowerCase(), field),
                Map.of(field, "already in use: %s".formatted(value))
        );
    }
}