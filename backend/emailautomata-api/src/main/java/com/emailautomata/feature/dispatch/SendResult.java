package com.emailautomata.feature.dispatch;

/**
 * Outcome of processing a whole dispatch: how many recipients were delivered
 * versus failed, and the dispatch's resulting status.
 */
public record SendResult(
        Long dispatchId,
        String status,
        int total,
        int sent,
        int failed
) {
}