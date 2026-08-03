package bg.softuni.bookshelf.service.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Request to lock a user account.
 *
 * @param reason            the administrative reason for the lock (required).
 * @param lockDurationHours the lock duration in hours; {@code null} (omitted) means a
 *                          <strong>permanent</strong> lock that the reconciliation job never lifts.
 *                          A positive value creates a temporary lock expiring after that many hours.
 */
public record LockUserRequestDto(
        @NotBlank(message = "{admin.user.lock.reason.notBlank}")
        String reason,

        @Positive(message = "{admin.user.lock.duration.positive}")
        Integer lockDurationHours
) {
}