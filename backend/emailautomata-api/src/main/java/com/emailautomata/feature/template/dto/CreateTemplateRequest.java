package com.emailautomata.feature.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTemplateRequest(

        @NotBlank(message = "Template name is required")
        @Size(min = 2, max = 140, message = "Name must be between 2 and 140 characters")
        String name,

        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject must be 255 characters or fewer")
        String subject,

        @NotBlank(message = "Body is required")
        @Size(max = 16_000_000, message = "Body is too large")
        String body
) {
}