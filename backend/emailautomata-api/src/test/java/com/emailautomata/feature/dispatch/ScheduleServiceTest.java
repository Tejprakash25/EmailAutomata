package com.emailautomata.feature.dispatch;

import com.emailautomata.core.error.BusinessException;
import com.emailautomata.core.error.IllegalStateTransitionException;
import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.dispatch.dto.ScheduleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScheduleServiceTest {

    private DispatchRepository dispatches;
    private DispatchRecipientRepository dispatchRecipients;
    private ScheduleService service;

    private final AuthenticatedUser principal = new AuthenticatedUser(1L, "u@example.com");

    @BeforeEach
    void setUp() {
        dispatches = mock(DispatchRepository.class);
        dispatchRecipients = mock(DispatchRecipientRepository.class);
        service = new ScheduleService(dispatches, dispatchRecipients);
        when(dispatchRecipients.findByDispatchIdOrderByIdAsc(anyLong())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Schedules a draft for a future time")
    void schedulesDraft() {
        Dispatch draft = Dispatch.draft(1L, null, "S", "B", 1);
        when(dispatches.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(draft));

        Instant future = Instant.now().plus(1, ChronoUnit.HOURS);
        var response = service.schedule(principal, 5L, new ScheduleRequest(future));

        assertThat(response.status()).isEqualTo("SCHEDULED");
        assertThat(response.scheduledAt()).isEqualTo(future);
    }

    @Test
    @DisplayName("Rejects a scheduled time in the past")
    void rejectsPastTime() {
        Dispatch draft = Dispatch.draft(1L, null, "S", "B", 1);
        when(dispatches.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(draft));

        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);

        assertThatThrownBy(() -> service.schedule(principal, 5L, new ScheduleRequest(past)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("future");
    }

    @Test
    @DisplayName("Cancelling a scheduled dispatch returns it to DRAFT")
    void cancelReturnsToDraft() {
        Dispatch draft = Dispatch.draft(1L, null, "S", "B", 1);
        draft.schedule(Instant.now().plus(1, ChronoUnit.HOURS));
        when(dispatches.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(draft));

        var response = service.cancel(principal, 5L);

        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.scheduledAt()).isNull();
    }

    @Test
    @DisplayName("Cannot schedule a dispatch that is not a draft")
    void cannotScheduleNonDraft() {
        Dispatch draft = Dispatch.draft(1L, null, "S", "B", 1);
        draft.schedule(Instant.now().plus(1, ChronoUnit.HOURS)); // now SCHEDULED
        when(dispatches.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() ->
                service.schedule(principal, 5L, new ScheduleRequest(Instant.now().plus(2, ChronoUnit.HOURS))))
                .isInstanceOf(IllegalStateTransitionException.class);
    }
}