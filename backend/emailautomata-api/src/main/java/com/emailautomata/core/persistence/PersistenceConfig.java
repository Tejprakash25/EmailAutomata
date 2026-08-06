package com.emailautomata.core.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Persistence-layer configuration.
 *
 * <p>Enables Spring Data auditing so {@link BaseEntity} timestamps populate
 * automatically on insert and update.</p>
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class PersistenceConfig {
}