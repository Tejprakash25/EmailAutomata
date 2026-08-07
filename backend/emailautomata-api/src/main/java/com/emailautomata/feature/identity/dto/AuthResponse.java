package com.emailautomata.feature.identity.dto;

/**
 * Issued credential plus the profile the client needs to render immediately,
 * so a successful login costs one round trip rather than two.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserProfileResponse user
) {

    public static AuthResponse of(String token, long expiresInSeconds, UserProfileResponse user) {
        return new AuthResponse(token, "Bearer", expiresInSeconds, user);
    }
}