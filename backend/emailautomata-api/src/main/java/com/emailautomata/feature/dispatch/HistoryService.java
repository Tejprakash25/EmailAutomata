package com.emailautomata.feature.dispatch;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.dispatch.dto.DeliveryBreakdownResponse;
import com.emailautomata.feature.dispatch.dto.DispatchHistoryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Serves the paginated, filterable sent-history view.
 *
 * <p>Filtering is composed from {@link DispatchSpecifications}; the delivery
 * breakdown for the whole page is fetched in one grouped query and joined in
 * memory, so a page of any size costs two queries total, not N+1.</p>
 */
@Service
public class HistoryService {

    private final DispatchRepository dispatches;
    private final DispatchRecipientRepository dispatchRecipients;

    public HistoryService(DispatchRepository dispatches,
                          DispatchRecipientRepository dispatchRecipients) {
        this.dispatches = dispatches;
        this.dispatchRecipients = dispatchRecipients;
    }

    @Transactional(readOnly = true)
    public Page<DispatchHistoryRow> history(AuthenticatedUser principal,
                                            DispatchStatus status,
                                            String search,
                                            Pageable pageable) {

        Specification<Dispatch> spec = DispatchSpecifications.ownedBy(principal.id())
                .and(DispatchSpecifications.hasStatus(status))
                .and(DispatchSpecifications.subjectContains(search));

        Page<Dispatch> page = dispatches.findAll(spec, pageable);

        if (page.isEmpty()) {
            return page.map(d -> DispatchHistoryRow.of(d, DeliveryBreakdownResponse.empty()));
        }

        // One grouped query for every dispatch on this page.
        List<Long> ids = page.getContent().stream().map(Dispatch::getId).toList();
        Map<Long, DeliveryBreakdownResponse> breakdowns = dispatchRecipients.breakdownFor(ids).stream()
                .collect(Collectors.toMap(
                        DeliveryBreakdown::getDispatchId,
                        b -> new DeliveryBreakdownResponse(
                                b.getSentCount(), b.getFailedCount(), b.getPendingCount()),
                        (a, b) -> a));

        return page.map(d -> DispatchHistoryRow.of(
                d, breakdowns.getOrDefault(d.getId(), DeliveryBreakdownResponse.empty())));
    }
}