package com.emailautomata.feature.dispatch.dto;

import java.util.List;

/**
 * Reports what a compose would produce without committing it — used by later
 * send/schedule flows to warn before acting. Returned by compose itself as the
 * created draft's readiness summary.
 */
public record ComposePreviewResponse(
        int totalRecipients,
        int readyRecipients,
        List<UnresolvedRecipient> unresolved
) {

    /** A recipient that cannot be rendered, and which fields they lack. */
    public record UnresolvedRecipient(String email, List<String> missingFields) {
    }

    public boolean isFullyReady() {
        return unresolved.isEmpty();
    }
}