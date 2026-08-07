package com.emailautomata.feature.dispatch;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.dispatch.transport.MailTransport;
import com.emailautomata.feature.dispatch.transport.MailTransportException;
import com.emailautomata.feature.dispatch.transport.OutboundMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SendServiceTest {

    private DispatchRepository dispatches;
    private DispatchRecipientRepository dispatchRecipients;
    private MailTransport transport;
    private SendService service;

    private final AuthenticatedUser principal = new AuthenticatedUser(1L, "u@example.com");

    @BeforeEach
    void setUp() {
        dispatches = mock(DispatchRepository.class);
        dispatchRecipients = mock(DispatchRecipientRepository.class);
        transport = mock(MailTransport.class);
        service = new SendService(dispatches, dispatchRecipients, transport);
        when(transport.name()).thenReturn("test");
    }

    private Dispatch draftWith(int recipientCount) {
        Dispatch d = Dispatch.draft(1L, null, "Subject", "Body", recipientCount);
        return d;
    }

    @Test
    @DisplayName("Delivers every clean recipient and marks the dispatch SENT")
    void deliversAll() {
        Dispatch dispatch = draftWith(2);
        when(dispatches.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(dispatch));
        when(dispatchRecipients.findByDispatchIdOrderByIdAsc(10L)).thenReturn(List.of(
                DispatchRecipient.rendered(10L, 1L, "a@x.com", "A", "Hi A", "Body A"),
                DispatchRecipient.rendered(10L, 2L, "b@x.com", "B", "Hi B", "Body B")));

        SendResult result = service.sendNow(principal, 10L);

        assertThat(result.sent()).isEqualTo(2);
        assertThat(result.failed()).isZero();
        assertThat(result.status()).isEqualTo("SENT");
    }

    @Test
    @DisplayName("Records a transport failure against just that recipient")
    void isolatesFailure() {
        Dispatch dispatch = draftWith(2);
        when(dispatches.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(dispatch));
        when(dispatchRecipients.findByDispatchIdOrderByIdAsc(10L)).thenReturn(List.of(
                DispatchRecipient.rendered(10L, 1L, "good@x.com", "A", "Hi A", "Body"),
                DispatchRecipient.rendered(10L, 2L, "bad@x.com", "B", "Hi B", "Body")));

        doThrow(new MailTransportException("bounce"))
                .when(transport).deliver(argThatEmailIs("bad@x.com"));

        SendResult result = service.sendNow(principal, 10L);

        assertThat(result.sent()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        // Any delivery at all still marks the dispatch SENT.
        assertThat(result.status()).isEqualTo("SENT");
    }

    @Test
    @DisplayName("Never transmits a row with an unresolved placeholder")
    void refusesUnresolved() {
        Dispatch dispatch = draftWith(1);
        when(dispatches.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(dispatch));
        when(dispatchRecipients.findByDispatchIdOrderByIdAsc(10L)).thenReturn(List.of(
                DispatchRecipient.rendered(10L, 1L, "a@x.com", "A", "Hi {{firstName}}", "Body")));

        SendResult result = service.sendNow(principal, 10L);

        assertThat(result.sent()).isZero();
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.status()).isEqualTo("FAILED");
    }

    private static OutboundMessage argThatEmailIs(String email) {
        return org.mockito.ArgumentMatchers.argThat(m -> m != null && email.equals(m.toEmail()));
    }
}