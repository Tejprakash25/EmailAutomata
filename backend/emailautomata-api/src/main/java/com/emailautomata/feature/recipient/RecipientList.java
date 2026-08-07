package com.emailautomata.feature.recipient;

import com.emailautomata.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A named grouping of recipients, so a whole cohort can be selected at once.
 */
@Entity
@Table(name = "recipient_lists")
public class RecipientList extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 140)
    private String name;

    protected RecipientList() {
    }

    private RecipientList(Long userId, String name) {
        this.userId = userId;
        this.name = name.trim();
    }

    public static RecipientList create(Long userId, String name) {
        return new RecipientList(userId, name);
    }

    public void rename(String name) {
        this.name = name.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}