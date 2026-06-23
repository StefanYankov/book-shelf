package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

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
