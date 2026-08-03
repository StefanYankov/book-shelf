package bg.softuni.bookshelf.service.user;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for determining the current status of a user's account.
 */
public interface AccountStatusService {

    /**
     * Determines if a user's account is currently active by inspecting their account status events.
     * An account is active if it has no status events, if the most recent event signifies an active
     * state (unlocked or unbanned), or if the most recent event is a temporary lock whose expiry has
     * already passed.
     *
     * @param userId The UUID of the user to check.
     * @return {@code true} if the user's account is active, {@code false} otherwise.
     */
    boolean isUserActive(UUID userId);

    /**
     * Computes active status for many users at once, from a single batched query, applying the same
     * rules as {@link #isUserActive(UUID)}. Users with no events default to active.
     *
     * @param userIds the users to evaluate.
     * @return a map of user id to active status; empty when {@code userIds} is empty.
     */
    Map<UUID, Boolean> getActiveStatus(Collection<UUID> userIds);
}