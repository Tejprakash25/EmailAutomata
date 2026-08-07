package com.emailautomata.feature.recipient.dto;

import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * The email is intentionally immutable after creation — it is the recipient's
 * identity within a user's book, and the unique constraint is built on it.
 * Changing address means deleting and re-adding.
 */
public record UpdateRecipientRequest(

        @Size(max = 140, message = "Display name must be 140 characters or fewer")
        String displayName,

        Long listId,

        Map<String, String> fields
) {

    public Map<String, String> fieldsOrEmpty() {
        return fields == null ? Map.of() : fields;
    }
}