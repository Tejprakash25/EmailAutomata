package com.emailautomata.core.security;

/**
 * The caller's identity for the duration of one request.
 *
 * <p>Deliberately minimal — id and email only. Nothing else in the token is
 * trusted for authorisation decisions, and no entity is attached, so a stale
 * or tampered token cannot smuggle state into the service layer.</p>
 */
public record AuthenticatedUser(Long id, String email) {
}