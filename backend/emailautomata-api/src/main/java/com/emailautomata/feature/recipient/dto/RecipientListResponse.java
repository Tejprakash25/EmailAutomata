package com.emailautomata.feature.recipient.dto;

import com.emailautomata.feature.recipient.RecipientList;

import java.time.Instant;

public record RecipientListResponse(
        Long id,
        String name,
        long recipientCount,
        Instant createdAt
) {

    public static RecipientListResponse from(RecipientList list, long recipientCount) {
        return new RecipientListResponse(list.getId(), list.getName(), recipientCount, list.getCreatedAt());
    }
}