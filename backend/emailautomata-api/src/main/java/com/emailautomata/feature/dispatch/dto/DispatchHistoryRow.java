package com.emailautomata.feature.dispatch.dto;

import com.emailautomata.feature.dispatch.Dispatch;

import java.time.Instant;

/**
 * One row in the sent-history table: the dispatch summary plus its delivery
 * breakdown. Body is omitted — never needed in a list.
 */
public record DispatchHistoryRow(
        Long id,
        String subject,
        String status,
        int recipientCount,
        DeliveryBreakdownResponse delivery,
        Instant scheduledAt,
        Instant sentAt,
        Instant createdAt
) {

    public static DispatchHistoryRow of(Dispatch d, DeliveryBreakdownResponse delivery) {
        return new DispatchHistoryRow(
                d.getId(),
                d.getSubject(),
                d.getStatus().name(),
                d.getRecipientCount(),
                delivery,
                d.getScheduledAt(),
                d.getSentAt(),
                d.getCreatedAt()
        );
    }
}