package bg.softuni.bookshelf.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduling entry points for account-maintenance jobs.
 */
@Component
@RequiredArgsConstructor
public class ScheduledMaintenanceTasks {

    private final AccountMaintenanceService accountMaintenanceService;

    /**
     * Nightly cleanup of expired verification and password-reset tokens.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredTokens() {
        accountMaintenanceService.purgeExpiredTokens();
    }

    /**
     * Periodically unlocks temporary locks whose window has elapsed.
     */
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 60_000)
    public void reconcileExpiredLocks() {
        accountMaintenanceService.reconcileExpiredLocks();
    }
}
