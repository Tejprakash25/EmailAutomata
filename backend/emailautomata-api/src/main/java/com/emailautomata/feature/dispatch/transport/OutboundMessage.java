package com.emailautomata.feature.dispatch.transport;

/**
 * One email ready to hand to a transport. Already rendered — the transport does
 * no templating, it only delivers.
 */
public record OutboundMessage(
        String toEmail,
        String toName,
        String subject,
        String body
) {
}