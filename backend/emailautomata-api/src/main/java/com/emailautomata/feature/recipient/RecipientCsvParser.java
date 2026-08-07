package com.emailautomata.feature.recipient;

import com.emailautomata.feature.recipient.dto.CsvImportResult.RowError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parses pasted CSV into candidate recipients.
 *
 * <p>Pure and dependency-free, so the parsing rules are unit-tested in
 * isolation from the database. The first row is a header: {@code email} is
 * required, {@code name} is optional, and every other column becomes a merge
 * field. That means a user's spreadsheet columns map straight onto template
 * placeholders with no separate mapping step.</p>
 */
public final class RecipientCsvParser {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public record ParsedRow(int line, String email, String displayName, Map<String, String> fields) {
    }

    public record ParseOutcome(List<ParsedRow> rows, List<RowError> errors) {
    }

    private RecipientCsvParser() {
    }

    public static ParseOutcome parse(String csv) {
        List<ParsedRow> rows = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();

        String[] lines = csv.replace("\r\n", "\n").replace("\r", "\n").split("\n");

        if (lines.length == 0 || lines[0].isBlank()) {
            errors.add(new RowError(1, "", "The CSV is empty."));
            return new ParseOutcome(rows, errors);
        }

        String[] headers = splitRow(lines[0]);
        int emailCol = indexOf(headers, "email");
        int nameCol = indexOf(headers, "name");

        if (emailCol < 0) {
            errors.add(new RowError(1, lines[0], "A column named 'email' is required."));
            return new ParseOutcome(rows, errors);
        }

        for (int i = 1; i < lines.length; i++) {
            int lineNumber = i + 1;
            String raw = lines[i];

            if (raw.isBlank()) {
                continue; // Blank lines are skipped silently, not reported as errors.
            }

            String[] cells = splitRow(raw);
            String email = cellAt(cells, emailCol).toLowerCase();

            if (email.isEmpty()) {
                errors.add(new RowError(lineNumber, raw, "Missing email."));
                continue;
            }
            if (!EMAIL.matcher(email).matches()) {
                errors.add(new RowError(lineNumber, email, "Not a valid email address."));
                continue;
            }

            String displayName = nameCol >= 0 ? cellAt(cells, nameCol) : "";

            // Every column that isn't email or name becomes a merge field.
            Map<String, String> fields = new LinkedHashMap<>();
            for (int c = 0; c < headers.length; c++) {
                if (c == emailCol || c == nameCol) {
                    continue;
                }
                String key = headers[c].trim();
                if (!key.isEmpty()) {
                    fields.put(key, cellAt(cells, c));
                }
            }

            rows.add(new ParsedRow(lineNumber, email, displayName, fields));
        }

        return new ParseOutcome(rows, errors);
    }

    private static String[] splitRow(String line) {
        String[] parts = line.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    private static int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    private static String cellAt(String[] cells, int index) {
        return index >= 0 && index < cells.length ? cells[index] : "";
    }
}