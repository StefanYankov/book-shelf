package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.entity.identity.AdminUser;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Component responsible for mapping between User entities and their corresponding DTOs.
 */
@Component
public class UserMapper {

    /**
     * Maps a {@link User} entity to a {@link UserProfileDto}.
     *
     * @param user The persistent User entity.
     * @return A {@link UserProfileDto} containing public profile information.
     */
    public UserProfileDto toUserProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    /**
     * Maps a {@link User} entity to an {@link AdminUserViewDto}.
     *
     * @param user The persistent User entity.
     * @return An {@link AdminUserViewDto} containing detailed user information for administrative purposes.
     */
    public AdminUserViewDto toAdminUserViewDto(User user) {
        AdminUserViewDto.AdminUserViewDtoBuilder builder = AdminUserViewDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName());

        if (user instanceof ApplicationUser appUser) {
            // Derives the user's current active status from their event history.
            boolean isActive = appUser.getStatusEvents().stream()
                    .max(Comparator.comparing(AccountStatusEvent::getCreatedAt))
                    .map(latestEvent -> latestEvent.getEventType() == StatusEventType.ACCOUNT_UNLOCKED || latestEvent.getEventType() == StatusEventType.ACCOUNT_UNBANNED)
                    .orElse(true);

            builder.isActive(isActive)
                   .isEmailVerified(appUser.isEmailVerified())
                   .role("ROLE_USER");
        } else if (user instanceof AdminUser) {
            builder.isActive(true)
                   .isEmailVerified(true)
                   .role("ROLE_ADMIN");
        }

        return builder.build();
    }
}
