package com.emailautomata.feature.template;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Page<EmailTemplate> findByUserId(Long userId, Pageable pageable);

    /**
     * Fetches by id and owner together, so a template belonging to another user
     * is indistinguishable from one that does not exist — the ownership rule is
     * enforced in the query, not in an afterthought check.
     */
    Optional<EmailTemplate> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    Optional<EmailTemplate> findByUserIdAndNameIgnoreCase(Long userId, String name);

    long countByUserId(Long userId);
}