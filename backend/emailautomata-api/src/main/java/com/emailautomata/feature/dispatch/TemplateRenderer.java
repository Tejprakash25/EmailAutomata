package com.emailautomata.feature.dispatch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders {@code {{placeholder}}} templates against a recipient's field map.
 *
 * <p>Pure and stateless. Uses the same token grammar as the template slice's
 * extractor, so what a template declares is exactly what this resolves.</p>
 *
 * <p>The key design point: {@link #render} reports the placeholders it could
 * not resolve rather than silently emitting a literal {@code {{firstName}}}
 * into an email. Compose treats any missing field as a reason not to send.</p>
 */
public final class TemplateRenderer {

    private static final Pattern TOKEN = Pattern.compile("\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*}}");

    public record Rendered(String text, Set<String> missing) {
    }

    private TemplateRenderer() {
    }

    /**
     * Substitutes every {@code {{name}}} with {@code fields.get(name)}.
     * A name absent from {@code fields} (or mapped to blank) is left in place
     * and recorded in {@code missing}.
     */
    public static Rendered render(String template, Map<String, String> fields) {
        if (template == null || template.isEmpty()) {
            return new Rendered(template == null ? "" : template, Set.of());
        }

        Set<String> missing = new LinkedHashSet<>();
        Matcher matcher = TOKEN.matcher(template);
        StringBuilder out = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = fields == null ? null : fields.get(key);

            if (value == null || value.isBlank()) {
                missing.add(key);
                matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(out, Matcher.quoteReplacement(value));
            }
        }
        matcher.appendTail(out);

        return new Rendered(out.toString(), missing);
    }

    /**
     * Renders subject and body together and returns the union of everything
     * missing across both — one verdict per recipient.
     */
    public static List<String> missingFields(String subject, String body, Map<String, String> fields) {
        Set<String> missing = new LinkedHashSet<>();
        missing.addAll(render(subject, fields).missing());
        missing.addAll(render(body, fields).missing());
        return new ArrayList<>(missing);
    }
}
