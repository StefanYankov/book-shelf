package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import bg.softuni.bookshelf.data.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Houses the business logic for scheduled account-maintenance jobs. Kept separate from the
 * scheduling entry points so the logic is unit-testable in isolation, and so the transactional
 * boundaries are applied through the Spring proxy rather than via self-invocation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountMaintenanceService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final AccountStatusEventRepository accountStatusEventRepository;

    /**
     * Deletes verification and password-reset tokens whose expiry has passed. These are single-use
     * and time-bound, so once expired they are dead rows that will never be redeemed.
     *
     * @return the number of tokens purged.
     */
    @Transactional
    public int purgeExpiredTokens() {
        int deleted = verificationTokenRepository.deleteByExpiryDateBefore(Instant.now());
        if (deleted > 0) {
            log.info("Scheduled token purge removed {} expired token(s).", deleted);
        }
        return deleted;
    }

    /**
     * Reconciles temporary account locks whose window has elapsed by recording a corresponding
     * unlock event for each. Permanent locks (null expiry) are never selected and are therefore
     * never auto-unlocked. The unlock events are system-generated: their {@code actor} is null,
     * which distinguishes automated reconciliation from an explicit administrative unlock.
     *
     * @return the number of locks reconciled.
     */
    @Transactional
    public int reconcileExpiredLocks() {
        List<AccountStatusEvent> expiredLocks = accountStatusEventRepository.findExpiredActiveLocks(Instant.now());

        for (AccountStatusEvent lock : expiredLocks) {
            AccountStatusEvent unlock = new AccountStatusEvent();
            unlock.setUser(lock.getUser());
            unlock.setActor(null);   // system-generated: no administrator performed this unlock
            unlock.setReason("Temporary lock expired");
            unlock.setEventType(StatusEventType.ACCOUNT_UNLOCKED);
            unlock.setExpiryDate(null);
            accountStatusEventRepository.save(unlock);
        }

        if (!expiredLocks.isEmpty()) {
            log.info("Scheduled lock reconciliation unlocked {} expired temporary lock(s).", expiredLocks.size());
        }
        return expiredLocks.size();
    }
}