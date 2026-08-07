package com.emailautomata.feature.recipient.dto;

import com.emailautomata.feature.recipient.Recipient;

import java.time.Instant;
import java.util.Map;

public record RecipientResponse(
        Long id,
        String email,
        String displayName,
        Long listId,
        Map<String, String> fields,
        Instant createdAt
) {

    public static RecipientResponse from(Recipient r) {
        return new RecipientResponse(
                r.getId(),
                r.getEmail(),
                r.getDisplayName(),
                r.getListId(),
                r.getFields(),
                r.getCreatedAt()
        );
    }
}