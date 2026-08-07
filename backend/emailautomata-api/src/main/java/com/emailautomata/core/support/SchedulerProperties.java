package com.emailautomata.core.support;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Tuning for the dispatch scheduler.
 *
 * <p>The poll interval and batch size are configuration, not magic numbers
 * buried in an annotation, so they can be tuned per environment without a
 * recompile.</p>
 */
@Validated
@ConfigurationProperties(prefix = "emailautomata.scheduler")
public record SchedulerProperties(

        boolean enabled,

        @Min(1000) long pollIntervalMs,

        @Min(1) int batchSize
) {
}