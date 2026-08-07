package com.emailautomata.feature.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispatchRecipientRepository extends JpaRepository<DispatchRecipient, Long> {

    List<DispatchRecipient> findByDispatchIdOrderByIdAsc(Long dispatchId);

    long countByDispatchId(Long dispatchId);
}