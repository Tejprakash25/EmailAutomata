package com.emailautomata.feature.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DispatchRecipientRepository extends JpaRepository<DispatchRecipient, Long> {

    List<DispatchRecipient> findByDispatchIdOrderByIdAsc(Long dispatchId);

    long countByDispatchId(Long dispatchId);

    /**
     * Delivery breakdown for many dispatches in one grouped query, so a history
     * page of N rows costs a single query rather than N status lookups.
     */
    @Query("""
            select dr.dispatchId as dispatchId,
                   sum(case when dr.deliveryStatus = com.emailautomata.feature.dispatch.DeliveryStatus.SENT then 1 else 0 end) as sentCount,
                   sum(case when dr.deliveryStatus = com.emailautomata.feature.dispatch.DeliveryStatus.FAILED then 1 else 0 end) as failedCount,
                   sum(case when dr.deliveryStatus = com.emailautomata.feature.dispatch.DeliveryStatus.PENDING then 1 else 0 end) as pendingCount
            from DispatchRecipient dr
            where dr.dispatchId in :dispatchIds
            group by dr.dispatchId
            """)
    List<DeliveryBreakdown> breakdownFor(@Param("dispatchIds") Collection<Long> dispatchIds);
}