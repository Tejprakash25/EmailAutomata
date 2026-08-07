package com.emailautomata.feature.recipient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipientListRepository extends JpaRepository<RecipientList, Long> {

    List<RecipientList> findByUserIdOrderByNameAsc(Long userId);

    Optional<RecipientList> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}