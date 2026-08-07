package com.emailautomata.feature.recipient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Page<Recipient> findByUserId(Long userId, Pageable pageable);

    Optional<Recipient> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndEmailIgnoreCase(Long userId, String email);

    /**
     * Returns which of the supplied emails this user already has. One query for
     * a whole CSV batch instead of a per-row existence check.
     */
    @Query("select r.email from Recipient r where r.userId = :userId and r.email in :emails")
    List<String> findExistingEmails(@Param("userId") Long userId, @Param("emails") List<String> emails);

    long countByUserId(Long userId);
}