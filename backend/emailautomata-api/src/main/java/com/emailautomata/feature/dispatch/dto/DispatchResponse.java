package com.emailautomata.feature.dispatch.dto;

import com.emailautomata.feature.dispatch.Dispatch;
import com.emailautomata.feature.dispatch.DispatchRecipient;

import java.time.Instant;
import java.util.List;

public record DispatchResponse(
        Long id,
        Long templateId,
        String subject,
        String body,
        String status,
        int recipientCount,
        Instant scheduledAt,
        Instant sentAt,
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
                d.getSentAt(),
                d.getCreatedAt(),
                recipients == null ? null : recipients.stream().map(DispatchRecipientResponse::from).toList()
        );
    }

    public static DispatchResponse summary(Dispatch d) {
        return of(d, null);
    }
}