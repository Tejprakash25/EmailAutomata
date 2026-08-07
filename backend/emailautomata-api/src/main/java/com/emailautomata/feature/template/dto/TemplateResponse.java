package com.emailautomata.feature.template.dto;

import com.emailautomata.feature.template.EmailTemplate;

import java.time.Instant;
import java.util.List;

/**
 * Full template projection, including the derived merge fields the compose
 * step will need.
 */
public record TemplateResponse(
        Long id,
        String name,
        String subject,
        String body,
        List<String> placeholders,
        Instant createdAt,
        Instant updatedAt
) {

    public static TemplateResponse from(EmailTemplate t) {
        return new TemplateResponse(
                t.getId(),
                t.getName(),
                t.getSubject(),
                t.getBody(),
                t.getPlaceholders(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}