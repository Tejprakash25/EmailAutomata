package com.emailautomata.feature.recipient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateRecipientRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 320, message = "Email is too long")
        String email,

        @Size(max = 140, message = "Display name must be 140 characters or fewer")
        String displayName,

        Long listId,

        /** Merge values keyed by placeholder name. Null is treated as empty. */
        Map<String, String> fields
) {

    public Map<String, String> fieldsOrEmpty() {
        return fields == null ? Map.of() : fields;
    }
}