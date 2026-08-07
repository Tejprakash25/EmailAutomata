package com.emailautomata.feature.dispatch;

import com.emailautomata.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One addressee within a dispatch, with content rendered specifically for them.
 *
 * <p>The rendered subject and body are frozen at compose time. Editing the
 * template or the recipient afterwards must never rewrite what was actually
 * queued or sent — the record is the truth of what went out.</p>
 */
@Entity
@Table(name = "dispatch_recipients")
public class DispatchRecipient extends BaseEntity {

    @Column(name = "dispatch_id", nullable = false, updatable = false)
    private Long dispatchId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "display_name", length = 140)
    private String displayName;

    @Column(name = "rendered_subject", nullable = false, length = 255)
    private String renderedSubject;

    @Column(name = "rendered_body", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String renderedBody;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 20)
    private DeliveryStatus deliveryStatus;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    protected DispatchRecipient() {
    }

    private DispatchRecipient(Long dispatchId, Long recipientId, String email, String displayName,
                              String renderedSubject, String renderedBody) {
        this.dispatchId = dispatchId;
        this.recipientId = recipientId;
        this.email = email;
        this.displayName = displayName;
        this.renderedSubject = renderedSubject;
        this.renderedBody = renderedBody;
        this.deliveryStatus = DeliveryStatus.PENDING;
    }

    public static DispatchRecipient rendered(Long dispatchId, Long recipientId, String email,
                                             String displayName, String renderedSubject, String renderedBody) {
        return new DispatchRecipient(dispatchId, recipientId, email, displayName, renderedSubject, renderedBody);
    }

    // Mutators for the send path in Commit 9/11.
    public void markSent(Instant at) {
        this.deliveryStatus = DeliveryStatus.SENT;
        this.deliveredAt = at;
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.deliveryStatus = DeliveryStatus.FAILED;
        this.failureReason = reason == null ? "Unknown error" : reason.substring(0, Math.min(reason.length(), 500));
    }

    public Long getDispatchId() {
        return dispatchId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRenderedSubject() {
        return renderedSubject;
    }

    public String getRenderedBody() {
        return renderedBody;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }
}