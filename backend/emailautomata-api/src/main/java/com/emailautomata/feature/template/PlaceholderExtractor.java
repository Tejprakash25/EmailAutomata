package com.emailautomata.feature.template;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls {@code {{mergeField}}} tokens out of template text.
 *
 * <p>Pure and stateless, so it is trivially unit-testable and carries no Spring
 * dependencies. The template's declared merge fields are derived from its
 * content rather than entered by hand, which means they cannot drift out of
 * sync with the text that uses them.</p>
 */
public final class PlaceholderExtractor {

    // {{ name }} — letters, digits, underscore; surrounding whitespace tolerated.
    private static final Pattern TOKEN = Pattern.compile("\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*}}");

    private PlaceholderExtractor() {
    }

    /**
     * Returns the distinct placeholder names across all supplied fragments, in
     * first-seen order. Order is stable so equal inputs always produce an equal
     * list — which keeps the persisted JSON deterministic.
     */
    public static List<String> extract(String... fragments) {
        LinkedHashSet<String> found = new LinkedHashSet<>();

        for (String fragment : fragments) {
            if (fragment == null) {
                continue;
            }
            Matcher matcher = TOKEN.matcher(fragment);
            while (matcher.find()) {
                found.add(matcher.group(1));
            }
        }

        return List.copyOf(found);
    }
}