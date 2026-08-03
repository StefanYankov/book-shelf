package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link AccountStatusEvent} entity.
 */
@Repository
public interface AccountStatusEventRepository extends JpaRepository<AccountStatusEvent, UUID> {

    /**
     * Finds the most recent status events for a specific user, ordered by creation date descending.
     * This query uses a {@code LEFT JOIN FETCH} to eagerly load the associated {@code actor} (the user who performed the action),
     * preventing N+1 query problems when accessing the actor's details.
     *
     * @param userId   The UUID of the user whose events are to be retrieved.
     * @param pageable A {@link Pageable} object, typically {@code PageRequest.of(0, 1)} to get only the most recent event.
     * @return A list of {@link AccountStatusEvent} entities with their actors initialized.
     */
    @Query("SELECT e FROM AccountStatusEvent e LEFT JOIN FETCH e.actor WHERE e.user.id = :userId ORDER BY e.createdAt DESC")
    List<AccountStatusEvent> findMostRecentEventForUser(UUID userId, Pageable pageable);

    /**
     * Finds temporary lock events whose expiry has passed and which are still the user's most
     * recent status event (i.e. the lock has not already been superseded by a later event).
     * <p>
     * This is the query behind the automated lock-reconciliation job. Its predicates encode the
     * definition of a "temporary lock that is due to expire":
     * <ul>
     *   <li>{@code eventType = ACCOUNT_LOCKED} — only locks are candidates.</li>
     *   <li>{@code expiryDate IS NOT NULL} — a NULL expiry means a <strong>permanent</strong> lock,
     *       which by design must never be auto-unlocked. Excluding NULL here is precisely what makes
     *       permanence work: permanent locks are structurally invisible to this query.</li>
     *   <li>{@code expiryDate < :now} — the temporary lock's window has elapsed.</li>
     *   <li>No later event exists for the same user — the lock is still in effect (not already
     *       unlocked, banned, etc. by a subsequent event).</li>
     * </ul>
     *
     * @param now the current instant; lock events with an expiry strictly before this are due.
     * @return the lock events that should be reconciled into unlock events.
     */
    @Query("""
            SELECT locked FROM AccountStatusEvent locked
            WHERE locked.eventType = bg.softuni.bookshelf.data.enums.StatusEventType.ACCOUNT_LOCKED
              AND locked.expiryDate IS NOT NULL
              AND locked.expiryDate < :now
              AND NOT EXISTS (
                    SELECT later FROM AccountStatusEvent later
                    WHERE later.user = locked.user
                      AND later.createdAt > locked.createdAt
              )
            """)
    List<AccountStatusEvent> findExpiredActiveLocks(@Param("now") Instant now);
}