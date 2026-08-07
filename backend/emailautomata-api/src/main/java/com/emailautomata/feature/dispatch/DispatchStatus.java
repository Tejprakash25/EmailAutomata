package com.emailautomata.feature.dispatch;

/**
 * Lifecycle of a whole dispatch.
 *
 * <p>DRAFT is produced by compose. SCHEDULED and SENDING/SENT/FAILED are
 * reached by later commits. Declaring the full lifecycle now means the state
 * machine those commits enforce is visible from the start.</p>
 */
public enum DispatchStatus {
    DRAFT,
    SCHEDULED,
    SENDING,
    SENT,
    FAILED
}