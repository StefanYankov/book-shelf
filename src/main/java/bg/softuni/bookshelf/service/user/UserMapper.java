package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import org.springframework.stereotype.Component;

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
}
