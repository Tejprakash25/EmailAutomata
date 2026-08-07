package com.emailautomata.feature.dispatch;

import com.emailautomata.feature.dispatch.transport.MailTransport;
import com.emailautomata.feature.dispatch.transport.MailTransportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SendServiceTest {

    private DispatchRepository dispatches;
    private DispatchRecipientRepository dispatchRecipients;
    private MailTransport transport;
    private SendService service;

    @BeforeEach
    void setUp() {
        dispatches = mock(DispatchRepository.class);
        dispatchRecipients = mock(DispatchRecipientRepository.class);
        transport = mock(MailTransport.class);
        service = new SendService(dispatches, dispatchRecipients, transport);
        when(transport.name()).thenReturn("test");
    }

    private Dispatch draftWith(int recipientCount) {
        return Dispatch.draft(1L, null, "Subject", "Body", recipientCount);
    }

    @Test
    @DisplayName("Delivers every clean recipient and marks the dispatch SENT")
    void deliversAll() {
        Dispatch dispatch = draftWith(2);
        // A freshly constructed entity has a null id; stub the row lookup on that.
        when(dispatchRecipients.findByDispatchIdOrderByIdAsc(dispatch.getId())).thenReturn(List.of(
                DispatchRecipient.rendered(10L, 1L, "a@x.com", "A", "Hi A", "Body A"),
                DispatchRecipient.rendered(10L, 2L, "b@x.com", "B", "Hi B", "Body B")));

        SendResult result = service.send(dispatch);

        assertThat(result.sent()).isEqualTo(2);
        assertThat(result.failed()).isZero();
        assertThat(result.status()).isEqualTo("SENT");
    }

    @Test
    @DisplayName("Records a transport failure against just that recipient")
    void isolatesFailure() {
        Dispatch dispatch = draftWith(2);
        when(dispatchRecipients.findByDispatchIdOrderByIdAsc(dispatch.getId())).thenReturn(List.of(
                DispatchRecipient.rendered(10L, 1L, "good@x.com", "A", "Hi A", "Body"),
                DispatchRecipient.rendered(10L, 2L, "bad@x.com", "B", "Hi B", "Body")));

        doThrow(new MailTransportException("bounce"))
                .when(transport).deliver(org.mockito.ArgumentMatchers.argThat(
                        m -> m != null && "bad@x.com".equals(m.toEmail())));

        SendResult result = service.send(dispatch);

        assertThat(result.sent()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.status()).isEqualTo("SENT");
    }

    @Test
    @DisplayName("Never transmits a row with an unresolved placeholder")
    void refusesUnresolved() {
        Dispatch dispatch = draftWith(1);
        when(dispatchRecipients.findByDispatchIdOrderByIdAsc(dispatch.getId())).thenReturn(List.of(
                DispatchRecipient.rendered(10L, 1L, "a@x.com", "A", "Hi {{firstName}}", "Body")));

        SendResult result = service.send(dispatch);

        assertThat(result.sent()).isZero();
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.status()).isEqualTo("FAILED");
    }
}