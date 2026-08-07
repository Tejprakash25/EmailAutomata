package com.emailautomata.feature.identity;

import com.emailautomata.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A registered account.
 *
 * <p>Maps onto the {@code users} table created by Flyway's V1 migration.
 * Because {@code ddl-auto} is {@code validate}, any divergence between this
 * class and that migration fails the application at startup.</p>
 *
 * <p>The password hash is package-private on the way out: nothing outside this
 * slice has a legitimate reason to read it, and it is never mapped into a DTO.</p>
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    protected User() {
        // Required by JPA.
    }

    private User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Creates a new account. The factory takes an already-hashed password so
     * this class cannot be constructed with a plaintext one by accident.
     */
    public static User register(String email, String passwordHash, String displayName) {
        return new User(email.toLowerCase().trim(), passwordHash, displayName.trim());
    }

    public String getEmail() {
        return email;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}