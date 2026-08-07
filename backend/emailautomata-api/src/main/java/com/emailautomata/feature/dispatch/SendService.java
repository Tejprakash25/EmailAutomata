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
 * <p>The same {@link #send(Dispatch)} core is used by both the instant-send
 * endpoint and the scheduler, so a scheduled send behaves identically to a
 * manual one — one code path, one set of guarantees.</p>
 */
@Service
public class SendService {

    private static final Logger log = LoggerFactory.getLogger(SendService.class);

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
     * Sends a dispatch the caller owns, immediately.
     */
    @Transactional
    public SendResult sendNow(AuthenticatedUser principal, Long dispatchId) {
        Dispatch dispatch = dispatches.findByIdAndUserId(dispatchId, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch", dispatchId));
        return send(dispatch);
    }

    /**
     * The shared send core. Assumes the dispatch has already been loaded and
     * that the caller is authorised (manual path checks ownership; scheduler
     * operates system-wide). Runs in its own transaction so the scheduler can
     * process each due dispatch independently.
     */
    @Transactional
    public SendResult send(Dispatch dispatch) {
        dispatch.beginSending(); // throws IllegalStateTransition if already sent

        List<DispatchRecipient> rows = dispatchRecipients.findByDispatchIdOrderByIdAsc(dispatch.getId());
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
                dispatch.getId(), transport.name(), sent, failed);

        return new SendResult(dispatch.getId(), dispatch.getStatus().name(), rows.size(), sent, failed);
    }

    private String firstUnresolved(String subject, String body) {
        var m = UNRESOLVED.matcher(subject);
        if (m.find()) return m.group();
        m = UNRESOLVED.matcher(body);
        return m.find() ? m.group() : null;
    }
}