package com.emailautomata.feature.dispatch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    Page<Dispatch> findByUserId(Long userId, Pageable pageable);

    Optional<Dispatch> findByIdAndUserId(Long id, Long userId);
}