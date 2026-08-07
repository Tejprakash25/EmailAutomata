package com.emailautomata.feature.dispatch.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ScheduleRequest(

        @NotNull(message = "A scheduled time is required")
        Instant scheduledAt
) {
}