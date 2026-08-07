package com.emailautomata.core.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The from-identity applied to every outbound message. Kept in our own
 * namespace rather than reusing spring.mail so the sender address is explicit
 * and validated.
 */
@Validated
@ConfigurationProperties(prefix = "emailautomata.mail")
public record MailProperties(

        @NotBlank String fromAddress,

        @NotBlank String fromName
) {
}