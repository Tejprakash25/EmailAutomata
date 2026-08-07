package com.emailautomata.feature.identity;

/**
 * Account lifecycle state.
 *
 * <p>Stored as a string rather than an ordinal, so inserting a value later
 * cannot silently reassign the meaning of existing rows.</p>
 */
public enum UserStatus {
    ACTIVE,
    SUSPENDED
}