package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserSecurityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

/**
 * Service interface for managing user profile data and administrative actions.
 */
public interface UserService {

    /**
     * Retrieves the public profile information for a specific user.
     *
     * @param userId The UUID of the user whose profile is to be retrieved.
     * @return A {@link UserProfileDto} containing the user's public data.
     * @throws bg.softuni.bookshelf.shared.exception.BusinessException if no user is found for the given ID.
     */
    UserProfileDto getProfile(UUID userId);

    /**
     * Updates the personal information (first and last name) for a specific user.
     *
     * @param userId The UUID of the user to update.
     * @param dto    A {@link UpdateProfileDto} containing the new data.
     * @throws bg.softuni.bookshelf.shared.exception.BusinessException if no user is found for the given ID.
     */
    void updateProfile(UUID userId, UpdateProfileDto dto);

    /**
     * Changes a user's password, flushes the new credentials, and returns a detached boundary projection.
     *
     * @param userId The UUID of the user changing their password.
     * @param dto    A {@link ChangePasswordDto} containing the current and new passwords.
     * @return The updated, detached {@link UserSecurityDto} projection.
     * @throws bg.softuni.bookshelf.shared.exception.BusinessException if no user is found for the given ID,
     *                                                                 or if the current password check fails.
     */
    UserSecurityDto changePassword(UUID userId, ChangePasswordDto dto);

    /**
     * Retrieves a paginated list of all users for administrative purposes.
     *
     * @param pageable The pagination information.
     * @return A {@link Page} of {@link AdminUserViewDto} objects.
     */
    @PreAuthorize("hasRole('ADMIN')")
    Page<AdminUserViewDto> getAllUsers(Pageable pageable);

    /**
     * Creates a new {@link bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent} to lock a user's account.
     *
     * @param userId  The UUID of the user to lock.
     * @param reason  The administrative reason for the action.
     * @param actorId The UUID of the administrator performing the action.
     * @throws bg.softuni.bookshelf.shared.exception.BusinessException if an administrator tries to self-lock.
     */
    @PreAuthorize("hasRole('ADMIN')")
    void lockUser(UUID userId, String reason, UUID actorId);

    /**
     * Creates a new {@link bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent} to unlock a user's account.
     *
     * @param userId  The UUID of the user to unlock.
     * @param reason  The administrative reason for the action.
     * @param actorId The UUID of the administrator performing the action.
     */
    @PreAuthorize("hasRole('ADMIN')")
    void unlockUser(UUID userId, String reason, UUID actorId);
}
