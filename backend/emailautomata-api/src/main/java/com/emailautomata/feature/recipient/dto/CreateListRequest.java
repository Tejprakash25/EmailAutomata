package com.emailautomata.feature.recipient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateListRequest(

        @NotBlank(message = "List name is required")
        @Size(min = 2, max = 140, message = "Name must be between 2 and 140 characters")
        String name
) {
}