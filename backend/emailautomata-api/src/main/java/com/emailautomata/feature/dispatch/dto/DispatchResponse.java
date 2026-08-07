package com.emailautomata.feature.dispatch.dto;

import com.emailautomata.feature.dispatch.Dispatch;
import com.emailautomata.feature.dispatch.DispatchRecipient;

import java.time.Instant;
import java.util.List;

/**
 * A dispatch with its per-recipient rows. Recipients are optional so list views
 * can omit them and detail views can include them.
 */
public record DispatchResponse(
        Long id,
        Long templateId,
        String subject,
        String body,
        String status,
        int recipientCount,
        Instant scheduledAt,
        Instant createdAt,
        List<DispatchRecipientResponse> recipients
) {

    public static DispatchResponse of(Dispatch d, List<DispatchRecipient> recipients) {
        return new DispatchResponse(
                d.getId(),
                d.getTemplateId(),
                d.getSubject(),
                d.getBody(),
                d.getStatus().name(),
                d.getRecipientCount(),
                d.getScheduledAt(),
                d.getCreatedAt(),
                recipients == null ? null : recipients.stream().map(DispatchRecipientResponse::from).toList()
        );
    }

    public static DispatchResponse summary(Dispatch d) {
        return of(d, null);
    }
}