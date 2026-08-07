package com.emailautomata.feature.identity.dto;

import com.emailautomata.feature.identity.User;

import java.time.Instant;

/**
 * Safe projection of an account. Constructed only from an entity, so a new
 * sensitive column cannot leak into responses by default.
 */
public record UserProfileResponse(
        Long id,
        String email,
        String displayName,
        Instant memberSince
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt()
        );
    }
}