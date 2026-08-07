package com.emailautomata.feature.dispatch.transport;

/**
 * A delivery failure for a single message. Its message becomes the recipient's
 * recorded failure reason.
 */
public class MailTransportException extends RuntimeException {

    public MailTransportException(String message) {
        super(message);
    }

    public MailTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}