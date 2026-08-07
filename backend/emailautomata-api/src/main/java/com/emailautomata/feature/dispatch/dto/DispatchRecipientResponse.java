package com.emailautomata.feature.dispatch.dto;

import com.emailautomata.feature.dispatch.DispatchRecipient;

import java.time.Instant;

public record DispatchRecipientResponse(
        Long id,
        String email,
        String displayName,
        String renderedSubject,
        String renderedBody,
        String deliveryStatus,
        String failureReason,
        Instant deliveredAt
) {

    public static DispatchRecipientResponse from(DispatchRecipient r) {
        return new DispatchRecipientResponse(
                r.getId(),
                r.getEmail(),
                r.getDisplayName(),
                r.getRenderedSubject(),
                r.getRenderedBody(),
                r.getDeliveryStatus().name(),
                r.getFailureReason(),
                r.getDeliveredAt()
        );
    }
}