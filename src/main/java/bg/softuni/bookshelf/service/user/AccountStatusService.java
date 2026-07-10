package bg.softuni.bookshelf.service.user;

import java.util.UUID;

/**
 * Service interface for determining the current status of a user's account.
 */
public interface AccountStatusService {

    /**
     * Determines if a user's account is currently active by inspecting their account status events.
     * An account is considered active if it has no status events or if the most recent event is
     * of a type that signifies an active state (e.g., UNLOCKED, UNBANNED).
     *
     * @param userId The UUID of the user to check.
     * @return {@code true} if the user's account is active, {@code false} otherwise.
     */
    boolean isUserActive(UUID userId);
}
