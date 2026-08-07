package com.emailautomata.feature.dispatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateRendererTest {

    @Test
    @DisplayName("Substitutes present fields and reports none missing")
    void substitutesPresentFields() {
        var result = TemplateRenderer.render(
                "Hi {{firstName}}, welcome to {{team}}.",
                Map.of("firstName", "Ada", "team", "Engineering"));

        assertThat(result.text()).isEqualTo("Hi Ada, welcome to Engineering.");
        assertThat(result.missing()).isEmpty();
    }

    @Test
    @DisplayName("Leaves a missing placeholder intact and records it")
    void recordsMissing() {
        var result = TemplateRenderer.render("Hi {{firstName}} {{lastName}}", Map.of("firstName", "Ada"));

        assertThat(result.text()).isEqualTo("Hi Ada {{lastName}}");
        assertThat(result.missing()).containsExactly("lastName");
    }

    @Test
    @DisplayName("Treats a blank field value as missing")
    void blankCountsAsMissing() {
        var result = TemplateRenderer.render("Role: {{role}}", Map.of("role", "  "));
        assertThat(result.missing()).containsExactly("role");
    }

    @Test
    @DisplayName("Unions missing fields across subject and body")
    void unionsAcrossSubjectAndBody() {
        var missing = TemplateRenderer.missingFields(
                "Hi {{firstName}}", "Your {{role}} starts {{date}}", Map.of("firstName", "Ada"));

        assertThat(missing).containsExactlyInAnyOrder("role", "date");
    }

    @Test
    @DisplayName("A value containing a dollar or backslash does not corrupt output")
    void handlesRegexSpecialReplacement() {
        var result = TemplateRenderer.render("Price: {{amount}}", Map.of("amount", "$5 \\ off"));
        assertThat(result.text()).isEqualTo("Price: $5 \\ off");
    }
}