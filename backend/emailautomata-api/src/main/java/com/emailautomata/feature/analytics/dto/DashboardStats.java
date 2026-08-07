package com.emailautomata.feature.analytics.dto;

import java.util.Map;

/**
 * Everything the dashboard needs, in one payload computed from aggregate
 * queries.
 *
 * @param deliveryRate percentage of attempted recipients that were delivered,
 *                     rounded to one decimal; 0 when nothing has been sent
 */
public record DashboardStats(
        long totalDispatches,
        long totalTemplates,
        long totalRecipients,
        long messagesSent,
        long messagesFailed,
        long messagesPending,
        double deliveryRate,
        Map<String, Long> statusBreakdown
) {
}