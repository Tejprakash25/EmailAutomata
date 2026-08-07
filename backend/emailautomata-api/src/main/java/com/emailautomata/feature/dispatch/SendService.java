package com.emailautomata.feature.dispatch;

import com.emailautomata.core.error.ResourceNotFoundException;
import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.dispatch.transport.MailTransport;
import com.emailautomata.feature.dispatch.transport.MailTransportException;
import com.emailautomata.feature.dispatch.transport.OutboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Sends a dispatch: transitions its status, delivers each recipient's rendered
 * message through the active {@link MailTransport}, and records every outcome
 * individually.
 *
 * <p>The transport is injected as an interface, so this service is identical
 * whether it is logging messages in dev or hitting SMTP in production.</p>
 */
@Service
public class SendService {

    private static final Logger log = LoggerFactory.getLogger(SendService.class);

    // Detects an unresolved placeholder surviving in rendered content.
    private static final Pattern UNRESOLVED = Pattern.compile("\\{\\{\\s*[a-zA-Z][a-zA-Z0-9_]*\\s*}}");

    private final DispatchRepository dispatches;
    private final DispatchRecipientRepository dispatchRecipients;
    private final MailTransport transport;

    public SendService(DispatchRepository dispatches,
                       DispatchRecipientRepository dispatchRecipients,
                       MailTransport transport) {
        this.dispatches = dispatches;
        this.dispatchRecipients = dispatchRecipients;
        this.transport = transport;
    }

    /**
     * Sends the dispatch immediately.
     *
     * <p>Runs in one transaction. Each recipient's delivery is attempted
     * independently: a transport failure is caught and recorded against that
     * recipient, never propagated, so the batch always completes and the record
     * reflects reality.</p>
     */
    @Transactional
    public SendResult sendNow(AuthenticatedUser principal, Long dispatchId) {
        Dispatch dispatch = dispatches.findByIdAndUserId(dispatchId, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch", dispatchId));

        dispatch.beginSending(); // throws IllegalStateTransition if already sent

        List<DispatchRecipient> rows = dispatchRecipients.findByDispatchIdOrderByIdAsc(dispatchId);
        int sent = 0;
        int failed = 0;

        for (DispatchRecipient row : rows) {
            String unresolved = firstUnresolved(row.getRenderedSubject(), row.getRenderedBody());

            if (unresolved != null) {
                row.markFailed("Unresolved placeholder: " + unresolved);
                failed++;
                continue;
            }

            try {
                transport.deliver(new OutboundMessage(
                        row.getEmail(), row.getDisplayName(),
                        row.getRenderedSubject(), row.getRenderedBody()));
                row.markSent(Instant.now());
                sent++;
            } catch (MailTransportException ex) {
                row.markFailed(ex.getMessage());
                failed++;
            }
        }

        dispatchRecipients.saveAll(rows);
        dispatch.completeSending(sent > 0, Instant.now());
        dispatches.save(dispatch);

        log.info("Dispatch {} sent via {}: {} delivered, {} failed",
                dispatchId, transport.name(), sent, failed);

        return new SendResult(dispatchId, dispatch.getStatus().name(), rows.size(), sent, failed);
    }

    /** Returns the first surviving {@code {{placeholder}}} in either field, or null. */
    private String firstUnresolved(String subject, String body) {
        var m = UNRESOLVED.matcher(subject);
        if (m.find()) return m.group();
        m = UNRESOLVED.matcher(body);
        return m.find() ? m.group() : null;
    }
}