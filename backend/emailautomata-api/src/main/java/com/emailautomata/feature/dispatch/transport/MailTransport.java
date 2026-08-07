package com.emailautomata.feature.dispatch.transport;

/**
 * Abstraction over the thing that actually delivers an email.
 *
 * <p>The send service depends on this interface, never on a concrete transport.
 * Swapping the log-based dev transport for real SMTP is a matter of which bean
 * is active — no service code changes. This is Dependency Inversion applied to
 * the one integration point most likely to differ across environments.</p>
 */
public interface MailTransport {

    /**
     * Delivers one message.
     *
     * @throws MailTransportException if delivery fails; the caller records the
     *                                reason against that recipient and moves on
     *                                to the next, so one bad address does not
     *                                sink the batch.
     */
    void deliver(OutboundMessage message);

    /** Short identifier for logs and the dispatch record. */
    String name();
}