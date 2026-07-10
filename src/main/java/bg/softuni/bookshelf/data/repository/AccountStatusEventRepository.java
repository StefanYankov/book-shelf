package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
