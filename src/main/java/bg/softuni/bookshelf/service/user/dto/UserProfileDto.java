package bg.softuni.bookshelf.service.user.dto;

import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName
) {
}
