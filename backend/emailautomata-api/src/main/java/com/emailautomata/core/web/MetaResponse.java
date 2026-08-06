package com.emailautomata.core.web;

import java.time.OffsetDateTime;

/**
 * Service identity payload, consumed by the client's system-check screen.
 */
public record MetaResponse(
        String product,
        String tagline,
        String apiVersion,
        String buildVersion,
        String environment,
        OffsetDateTime serverTime
) {
}
