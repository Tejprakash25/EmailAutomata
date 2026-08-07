package com.emailautomata.feature.recipient;

import com.emailautomata.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.TreeMap;

/**
 * A person a user can write to, plus the merge values that personalise a
 * template for them.
 *
 * <p>The list membership is a scalar {@code list_id} rather than an
 * association, matching the template slice's approach — nothing here needs to
 * navigate the object graph.</p>
 */
@Entity
@Table(name = "recipients")
public class Recipient extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "list_id")
    private Long listId;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "display_name", length = 140)
    private String displayName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fields", nullable = false, columnDefinition = "JSON")
    private Map<String, String> fields;

    protected Recipient() {
    }

    private Recipient(Long userId, Long listId, String email, String displayName,
                      Map<String, String> fields) {
        this.userId = userId;
        this.listId = listId;
        this.email = email.toLowerCase().trim();
        this.displayName = displayName == null || displayName.isBlank() ? null : displayName.trim();
        // TreeMap keeps the persisted JSON key-ordered, so equal data serialises
        // identically regardless of input order.
        this.fields = new TreeMap<>(fields);
    }

    public static Recipient create(Long userId, Long listId, String email,
                                   String displayName, Map<String, String> fields) {
        return new Recipient(userId, listId, email, displayName, fields);
    }

    public void update(Long listId, String displayName, Map<String, String> fields) {
        this.listId = listId;
        this.displayName = displayName == null || displayName.isBlank() ? null : displayName.trim();
        this.fields = new TreeMap<>(fields);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getListId() {
        return listId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, String> getFields() {
        return fields;
    }
}