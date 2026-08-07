package com.emailautomata.feature.template;

import com.emailautomata.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/**
 * A reusable email template owned by one user.
 *
 * <p>The owner id is held as a plain {@code Long} rather than a {@code @ManyToOne}
 * association. Nothing in this slice needs to navigate to the User, and a scalar
 * foreign key keeps the template loadable without dragging the account graph
 * into every query.</p>
 */
@Entity
@Table(name = "email_templates")
public class EmailTemplate extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 140)
    private String name;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String body;

    // Persaved as a JSON array. Hibernate maps List<String> <-> JSON via SqlTypes.JSON.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "placeholders", nullable = false, columnDefinition = "JSON")
    private List<String> placeholders;

    protected EmailTemplate() {
        // Required by JPA.
    }

    private EmailTemplate(Long userId, String name, String subject, String body) {
        this.userId = userId;
        applyContent(name, subject, body);
    }

    public static EmailTemplate create(Long userId, String name, String subject, String body) {
        return new EmailTemplate(userId, name, subject, body);
    }

    /**
     * Re-applies content and re-derives placeholders. The two always change
     * together, so a single method owns the invariant that declared merge
     * fields match the current text.
     */
    public void applyContent(String name, String subject, String body) {
        this.name = name.trim();
        this.subject = subject.trim();
        this.body = body;
        this.placeholders = PlaceholderExtractor.extract(subject, body);
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public List<String> getPlaceholders() {
        return placeholders;
    }
}