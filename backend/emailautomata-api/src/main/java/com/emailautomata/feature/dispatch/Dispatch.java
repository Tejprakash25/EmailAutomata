package com.emailautomata.feature.dispatch;

import com.emailautomata.core.error.IllegalStateTransitionException;
import com.emailautomata.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One composed send. Holds the source subject/body (with placeholders intact)
 * for the record; the resolved per-recipient content lives on
 * {@link DispatchRecipient}.
 */
@Entity
@Table(name = "dispatches")
public class Dispatch extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DispatchStatus status;

    @Column(name = "recipient_count", nullable = false)
    private int recipientCount;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected Dispatch() {
    }

    private Dispatch(Long userId, Long templateId, String subject, String body, int recipientCount) {
        this.userId = userId;
        this.templateId = templateId;
        this.subject = subject;
        this.body = body;
        this.status = DispatchStatus.DRAFT;
        this.recipientCount = recipientCount;
    }

    public static Dispatch draft(Long userId, Long templateId, String subject, String body, int recipientCount) {
        return new Dispatch(userId, templateId, subject, body, recipientCount);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public DispatchStatus getStatus() {
        return status;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    /** DRAFT or SCHEDULED → SENDING. Guards against re-sending a finished dispatch. */
    public void beginSending() {
        if (status != DispatchStatus.DRAFT && status != DispatchStatus.SCHEDULED) {
            throw new IllegalStateTransitionException(
                    "dispatch", status.name(), DispatchStatus.SENDING.name());
        }
        this.status = DispatchStatus.SENDING;
    }

    /** SENDING → SENT/FAILED, stamping completion time. */
    public void completeSending(boolean anyDelivered, Instant at) {
        this.status = anyDelivered ? DispatchStatus.SENT : DispatchStatus.FAILED;
        this.sentAt = at;
    }

    // Package-scoped transitions used by the schedule path in the next commit.
    void markStatus(DispatchStatus status) {
        this.status = status;
    }

    void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
}