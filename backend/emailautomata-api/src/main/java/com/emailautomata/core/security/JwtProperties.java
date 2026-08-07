package com.emailautomata.core.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Token issuance settings.
 *
 * <p>The minimum secret length is enforced at startup rather than discovered at
 * the first login attempt: HS256 requires a key of at least 256 bits, and a
 * short secret is a silent security downgrade otherwise.</p>
 */
@Validated
@ConfigurationProperties(prefix = "emailautomata.jwt")
public record JwtProperties(

        @NotBlank
        @Size(min = 32, message = "JWT secret must be at least 32 characters for HS256")
        String secret,

        @NotBlank String issuer,

        @Min(1) long expiryMinutes
) {
}