package bg.softuni.bookshelf.service.user.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AdminUserViewDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean isActive,
        boolean isEmailVerified,
        String role
) {
}
