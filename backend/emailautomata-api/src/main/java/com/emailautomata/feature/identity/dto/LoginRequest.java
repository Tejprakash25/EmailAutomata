package com.emailautomata.feature.identity.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login payload.
 *
 * <p>No format or length constraints beyond presence: rejecting a malformed
 * email here would tell an attacker which addresses are structurally valid,
 * and the credential check answers the only question that matters anyway.</p>
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}