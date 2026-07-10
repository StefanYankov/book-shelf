package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;

import java.util.UUID;

/**
 * Service interface for managing user profile data.
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
     * Changes the password for a specific user after verifying their current password.
     *
     * @param userId The UUID of the user changing their password.
     * @param dto    A {@link ChangePasswordDto} containing the current and new passwords.
     * @throws bg.softuni.bookshelf.shared.exception.BusinessException if no user is found for the given ID,
     *                                                                 or if the provided current password is incorrect.
     */
    void changePassword(UUID userId, ChangePasswordDto dto);
}
