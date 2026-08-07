package com.emailautomata.feature.dispatch;

import com.emailautomata.core.support.SchedulerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Polls for due dispatches and sends them through the shared send core.
 *
 * <p>Each due dispatch is sent in its own transaction (via {@link SendService},
 * which is {@code @Transactional}). An {@link OptimisticLockingFailureException}
 * means another actor — a second poll tick, or a manual send — already claimed
 * that dispatch, so this tick simply skips it. That is exactly how the version
 * column prevents a double-send.</p>
 */
@Component
public class DispatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DispatchScheduler.class);

    private final DispatchRepository dispatches;
    private final SendService sendService;
    private final SchedulerProperties properties;

    public DispatchScheduler(DispatchRepository dispatches,
                             SendService sendService,
                             SchedulerProperties properties) {
        this.dispatches = dispatches;
        this.sendService = sendService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${emailautomata.scheduler.poll-interval-ms}")
    public void pollAndSend() {
        if (!properties.enabled()) {
            return;
        }

        List<Dispatch> due = dispatches.findDue(Instant.now(), PageRequest.of(0, properties.batchSize()));
        if (due.isEmpty()) {
            return;
        }

        log.info("Scheduler found {} due dispatch(es)", due.size());

        for (Dispatch dispatch : due) {
            try {
                sendService.send(dispatch);
            } catch (OptimisticLockingFailureException ex) {
                // Another actor claimed it first; safe to skip.
                log.debug("Dispatch {} already claimed, skipping", dispatch.getId());
            } catch (Exception ex) {
                // A failure on one dispatch must not stop the rest of the batch.
                log.error("Scheduled send failed for dispatch {}", dispatch.getId(), ex);
            }
        }
    }
}