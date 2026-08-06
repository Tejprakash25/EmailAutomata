package com.emailautomata.core.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Type-safe binding for all {@code emailautomata.*} configuration.
 *
 * <p>Validated at startup so a misconfigured environment fails fast and loudly
 * rather than surfacing as a confusing runtime error later.</p>
 */
@Validated
@ConfigurationProperties(prefix = "emailautomata")
public record AppProperties(

        @NotBlank String apiVersion,

        @NotBlank String buildVersion,

        Cors cors
) {

    public record Cors(
            @NotEmpty List<String> allowedOrigins
    ) {
    }
}
