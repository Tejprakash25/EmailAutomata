package com.emailautomata.feature.template.dto;

import com.emailautomata.feature.template.EmailTemplate;

import java.time.Instant;

/**
 * Lightweight projection for list views — omits the body, which can be large
 * and is never needed in a list. Keeps the list endpoint's payload small
 * regardless of how big the templates are.
 */
public record TemplateSummaryResponse(
        Long id,
        String name,
        String subject,
        int placeholderCount,
        Instant updatedAt
) {

    public static TemplateSummaryResponse from(EmailTemplate t) {
        return new TemplateSummaryResponse(
                t.getId(),
                t.getName(),
                t.getSubject(),
                t.getPlaceholders().size(),
                t.getUpdatedAt()
        );
    }
}