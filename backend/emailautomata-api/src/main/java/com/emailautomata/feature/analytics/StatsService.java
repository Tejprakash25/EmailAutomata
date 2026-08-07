package com.emailautomata.feature.analytics;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.analytics.dto.DashboardStats;
import com.emailautomata.feature.dispatch.DispatchRecipientRepository;
import com.emailautomata.feature.dispatch.DispatchRepository;
import com.emailautomata.feature.dispatch.DispatchStatus;
import com.emailautomata.feature.recipient.RecipientRepository;
import com.emailautomata.feature.template.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Computes the dashboard summary.
 *
 * <p>Every figure comes from a count or grouped-count query — no entity is
 * loaded to be tallied in Java. Reaches across the dispatch, template and
 * recipient slices' repositories in one direction, consistent with the rest of
 * the analytics slice.</p>
 */
@Service
public class StatsService {

    private final DispatchRepository dispatches;
    private final DispatchRecipientRepository dispatchRecipients;
    private final TemplateRepository templates;
    private final RecipientRepository recipients;

    public StatsService(DispatchRepository dispatches,
                        DispatchRecipientRepository dispatchRecipients,
                        TemplateRepository templates,
                        RecipientRepository recipients) {
        this.dispatches = dispatches;
        this.dispatchRecipients = dispatchRecipients;
        this.templates = templates;
        this.recipients = recipients;
    }

    @Transactional(readOnly = true)
    public DashboardStats forUser(AuthenticatedUser principal) {
        Long userId = principal.id();

        long totalDispatches = dispatches.countByUserId(userId);
        long totalTemplates = templates.countByUserId(userId);
        long totalRecipients = recipients.countByUserId(userId);

        var totals = dispatchRecipients.deliveryTotalsFor(userId);
        long sent = nullSafe(totals == null ? null : totals.getSent());
        long failed = nullSafe(totals == null ? null : totals.getFailed());
        long pending = nullSafe(totals == null ? null : totals.getPending());

        long attempted = sent + failed;
        double deliveryRate = attempted == 0 ? 0.0
                : Math.round((sent * 1000.0) / attempted) / 10.0;

        // Status breakdown, defaulting every status to zero so the UI can render
        // a stable set of buckets even before anything exists.
        Map<DispatchStatus, Long> counts = new EnumMap<>(DispatchStatus.class);
        for (DispatchStatus status : DispatchStatus.values()) {
            counts.put(status, 0L);
        }
        dispatches.countByStatusFor(userId)
                .forEach(row -> counts.put(row.getStatus(), row.getTotal()));

        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        counts.forEach((status, total) -> statusBreakdown.put(status.name(), total));

        return new DashboardStats(
                totalDispatches, totalTemplates, totalRecipients,
                sent, failed, pending, deliveryRate, statusBreakdown);
    }

    private static long nullSafe(Long value) {
        return value == null ? 0L : value;
    }
}