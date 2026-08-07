package com.emailautomata.feature.dispatch;

/**
 * Per-recipient delivery outcome — the product's core vocabulary, fixed here
 * and surfaced with the same three colours everywhere in the UI.
 */
public enum DeliveryStatus {
    PENDING,
    SENT,
    FAILED
}