package com.emailautomata.feature.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderExtractorTest {

    @Test
    @DisplayName("Extracts distinct placeholders across subject and body in first-seen order")
    void extractsDistinctInOrder() {
        var result = PlaceholderExtractor.extract(
                "Welcome {{firstName}}",
                "Hi {{firstName}}, your {{role}} role starts {{startDate}}.");

        assertThat(result).containsExactly("firstName", "role", "startDate");
    }

    @Test
    @DisplayName("Tolerates surrounding whitespace inside the braces")
    void toleratesWhitespace() {
        assertThat(PlaceholderExtractor.extract("{{ firstName }}"))
                .containsExactly("firstName");
    }

    @Test
    @DisplayName("Ignores malformed or empty tokens")
    void ignoresMalformed() {
        assertThat(PlaceholderExtractor.extract("{{}} {{ }} {{1bad}} {single} {{good}}"))
                .containsExactly("good");
    }

    @Test
    @DisplayName("Returns an empty list when there are no placeholders")
    void emptyWhenNone() {
        assertThat(PlaceholderExtractor.extract("Plain subject", "Plain body")).isEmpty();
    }

    @Test
    @DisplayName("Handles null fragments without failing")
    void handlesNull() {
        assertThat(PlaceholderExtractor.extract("Hi {{name}}", null)).containsExactly("name");
    }
}