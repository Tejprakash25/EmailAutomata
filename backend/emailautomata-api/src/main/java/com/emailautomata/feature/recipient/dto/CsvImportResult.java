package com.emailautomata.feature.recipient.dto;

import java.util.List;

/**
 * The outcome of a bulk import, reported per row.
 *
 * <p>Partial success is a first-class result: good rows are saved and bad rows
 * are returned with the line number and reason, so the user fixes three rows
 * rather than re-submitting four hundred.</p>
 */
public record CsvImportResult(
        int imported,
        int skipped,
        List<RowError> errors
) {

    public record RowError(int line, String value, String reason) {
    }
}