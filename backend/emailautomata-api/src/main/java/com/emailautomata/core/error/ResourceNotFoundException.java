package com.emailautomata.core.error;

/**
 * Thrown when a resource does not exist, or exists but is not owned by the
 * caller.
 *
 * <p>Both cases deliberately return 404 rather than 403. Distinguishing them
 * would confirm that a given id exists, letting an attacker enumerate the
 * identifier space.</p>
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "%s %s was not found.".formatted(resource, identifier));
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}