package com.emailautomata.core.error;

/**
 * Thrown when an operation is invalid for the resource's current state — for
 * example, cancelling a dispatch that has already been sent.
 *
 * <p>Introduced now so the dispatch lifecycle in later commits has a defined
 * failure mode rather than an ad-hoc {@code IllegalStateException}.</p>
 */
public class IllegalStateTransitionException extends BusinessException {

    public IllegalStateTransitionException(String resource, String from, String to) {
        super(
                ErrorCode.ILLEGAL_STATE_TRANSITION,
                "A %s cannot move from %s to %s.".formatted(resource.toLowerCase(), from, to)
        );
    }
}