package com.emailautomata.feature.dispatch;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Composable query predicates for the history view.
 *
 * <p>Each filter is an independent {@link Specification}. They combine with
 * {@code and(...)}, so any mix of status filter and subject search works
 * without a bespoke repository method per combination — and a new filter is one
 * more method here, not a combinatorial explosion of query methods.</p>
 */
public final class DispatchSpecifications {

    private DispatchSpecifications() {
    }

    /** Mandatory owner scope — the base every history query starts from. */
    public static Specification<Dispatch> ownedBy(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    /** Optional exact status match. A null status matches everything. */
    public static Specification<Dispatch> hasStatus(DispatchStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /** Optional case-insensitive subject search. Blank matches everything. */
    public static Specification<Dispatch> subjectContains(String term) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(term)) {
                return cb.conjunction();
            }
            String pattern = "%" + term.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("subject")), pattern);
        };
    }
}