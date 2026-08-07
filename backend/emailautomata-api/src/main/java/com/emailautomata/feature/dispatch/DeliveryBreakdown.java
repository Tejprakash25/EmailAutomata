package com.emailautomata.feature.dispatch;

/**
 * Aggregate delivery tally for a single dispatch, projected straight from a
 * grouped count query.
 */
public interface DeliveryBreakdown {

    Long getDispatchId();

    long getSentCount();

    long getFailedCount();

    long getPendingCount();
}