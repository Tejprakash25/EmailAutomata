package com.emailautomata.feature.dispatch;

import com.emailautomata.core.error.BusinessException;
import com.emailautomata.core.error.ErrorCode;
import com.emailautomata.core.error.ResourceNotFoundException;
import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.dispatch.dto.DispatchResponse;
import com.emailautomata.feature.dispatch.dto.ScheduleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Owns the schedule and cancel transitions for a user's dispatch.
 */
@Service
public class ScheduleService {

    private final DispatchRepository dispatches;
    private final DispatchRecipientRepository dispatchRecipients;

    public ScheduleService(DispatchRepository dispatches,
                           DispatchRecipientRepository dispatchRecipients) {
        this.dispatches = dispatches;
        this.dispatchRecipients = dispatchRecipients;
    }

    @Transactional
    public DispatchResponse schedule(AuthenticatedUser principal, Long id, ScheduleRequest request) {
        Dispatch dispatch = dispatches.findByIdAndUserId(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch", id));

        if (request.scheduledAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Scheduled time must be in the future.");
        }

        dispatch.schedule(request.scheduledAt());
        dispatches.save(dispatch);

        return DispatchResponse.of(dispatch, dispatchRecipients.findByDispatchIdOrderByIdAsc(id));
    }

    @Transactional
    public DispatchResponse cancel(AuthenticatedUser principal, Long id) {
        Dispatch dispatch = dispatches.findByIdAndUserId(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch", id));

        dispatch.cancelSchedule();
        dispatches.save(dispatch);

        return DispatchResponse.of(dispatch, dispatchRecipients.findByDispatchIdOrderByIdAsc(id));
    }
}