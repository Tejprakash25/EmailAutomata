package com.emailautomata.feature.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DispatchRecipientRepository extends JpaRepository<DispatchRecipient, Long> {

    List<DispatchRecipient> findByDispatchIdOrderByIdAsc(Long dispatchId);

    long countByDispatchId(Long dispatchId);

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

    /**
     * Delivery totals across all of a user's dispatches, computed in the
     * database. Joins to the parent dispatch to scope by owner.
     */
    @Query("""
            select
                sum(case when dr.deliveryStatus = com.emailautomata.feature.dispatch.DeliveryStatus.SENT then 1 else 0 end) as sent,
                sum(case when dr.deliveryStatus = com.emailautomata.feature.dispatch.DeliveryStatus.FAILED then 1 else 0 end) as failed,
                sum(case when dr.deliveryStatus = com.emailautomata.feature.dispatch.DeliveryStatus.PENDING then 1 else 0 end) as pending,
                count(dr) as total
            from DispatchRecipient dr
            join Dispatch d on d.id = dr.dispatchId
            where d.userId = :userId
            """)
    DeliveryTotals deliveryTotalsFor(@Param("userId") Long userId);

    interface DeliveryTotals {
        Long getSent();
        Long getFailed();
        Long getPending();
        Long getTotal();
    }
}