package com.emailautomata.feature.dispatch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DispatchRepository
        extends JpaRepository<Dispatch, Long>, JpaSpecificationExecutor<Dispatch> {

    Page<Dispatch> findByUserId(Long userId, Pageable pageable);

    Optional<Dispatch> findByIdAndUserId(Long id, Long userId);

    @Query("""
            select d from Dispatch d
            where d.status = com.emailautomata.feature.dispatch.DispatchStatus.SCHEDULED
              and d.scheduledAt <= :now
            order by d.scheduledAt asc
            """)
    List<Dispatch> findDue(@Param("now") Instant now, Pageable pageable);
}