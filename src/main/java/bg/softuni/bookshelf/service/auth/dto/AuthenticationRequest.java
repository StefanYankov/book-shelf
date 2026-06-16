package bg.softuni.bookshelf.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Defines the API contract for a user login request.
 */
public record AuthenticationRequest(
        @NotBlank(message = "{validation.auth.username.notblank}")
        String username,

        @NotBlank(message = "{validation.auth.password.notblank}")
        String password
) {
}
