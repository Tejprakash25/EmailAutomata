package com.emailautomata.feature.recipient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecipientCsvParserTest {

    @Test
    @DisplayName("Parses header, maps extra columns to merge fields")
    void parsesRowsAndFields() {
        String csv = """
                email,name,firstName,role
                ada@example.com,Ada Lovelace,Ada,Engineer
                grace@example.com,Grace Hopper,Grace,Admiral
                """;

        var outcome = RecipientCsvParser.parse(csv);

        assertThat(outcome.errors()).isEmpty();
        assertThat(outcome.rows()).hasSize(2);
        assertThat(outcome.rows().get(0).email()).isEqualTo("ada@example.com");
        assertThat(outcome.rows().get(0).displayName()).isEqualTo("Ada Lovelace");
        assertThat(outcome.rows().get(0).fields())
                .containsEntry("firstName", "Ada")
                .containsEntry("role", "Engineer");
    }

    @Test
    @DisplayName("Reports invalid emails by line number without dropping good rows")
    void reportsInvalidByLine() {
        String csv = """
                email,name
                ada@example.com,Ada
                not-an-email,Bob
                grace@example.com,Grace
                """;

        var outcome = RecipientCsvParser.parse(csv);

        assertThat(outcome.rows()).hasSize(2);
        assertThat(outcome.errors()).hasSize(1);
        assertThat(outcome.errors().get(0).line()).isEqualTo(3);
        assertThat(outcome.errors().get(0).reason()).contains("valid email");
    }

    @Test
    @DisplayName("Fails clearly when the email column is absent")
    void requiresEmailColumn() {
        var outcome = RecipientCsvParser.parse("name,role\nAda,Engineer");

        assertThat(outcome.rows()).isEmpty();
        assertThat(outcome.errors()).hasSize(1);
        assertThat(outcome.errors().get(0).reason()).contains("email");
    }

    @Test
    @DisplayName("Skips blank lines silently")
    void skipsBlankLines() {
        String csv = "email\nada@example.com\n\n\ngrace@example.com\n";
        var outcome = RecipientCsvParser.parse(csv);

        assertThat(outcome.rows()).hasSize(2);
        assertThat(outcome.errors()).isEmpty();
    }

    @Test
    @DisplayName("Tolerates Windows line endings")
    void toleratesCrlf() {
        var outcome = RecipientCsvParser.parse("email\r\nada@example.com\r\n");
        assertThat(outcome.rows()).hasSize(1);
        assertThat(outcome.rows().get(0).email()).isEqualTo("ada@example.com");
    }
}