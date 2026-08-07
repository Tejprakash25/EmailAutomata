package com.emailautomata.feature.dispatch.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development transport. "Delivers" by logging the message.
 *
 * <p>Active by default (when {@code emailautomata.mail.transport} is 'log' or
 * unset). It lets the entire send pipeline — status transitions, per-recipient
 * outcomes, history — be exercised end to end with no SMTP server, so anyone
 * cloning the repo sees sends succeed immediately.</p>
 */
@Component
@ConditionalOnProperty(name = "emailautomata.mail.transport", havingValue = "log", matchIfMissing = true)
public class LogMailTransport implements MailTransport {

    private static final Logger log = LoggerFactory.getLogger(LogMailTransport.class);

    @Override
    public void deliver(OutboundMessage message) {
        log.info("[LogMailTransport] → {} <{}> | {}",
                message.toName() == null ? "" : message.toName(),
                message.toEmail(),
                message.subject());
        // A deliberate failure hook for local testing: any address at
        // example.invalid is treated as a hard bounce.
        if (message.toEmail().endsWith("@example.invalid")) {
            throw new MailTransportException("Simulated bounce for " + message.toEmail());
        }
    }

    @Override
    public String name() {
        return "log";
    }
}