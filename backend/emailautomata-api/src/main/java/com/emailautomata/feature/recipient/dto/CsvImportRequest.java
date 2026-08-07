package com.emailautomata.feature.recipient.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * A raw CSV payload plus an optional target list. The CSV is pasted or
 * uploaded client-side and sent as text, so import needs no multipart handling.
 */
public record CsvImportRequest(

        @NotBlank(message = "CSV content is required")
        String csv,

        Long listId
) {
}