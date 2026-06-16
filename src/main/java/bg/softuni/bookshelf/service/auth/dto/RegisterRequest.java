package bg.softuni.bookshelf.service.auth.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Defines the API contract for a new user registration.
 */
public record RegisterRequest(
        @NotBlank(message = "{validation.auth.firstname.notblank}")
        String firstName,

        @NotBlank(message = "{validation.auth.lastname.notblank}")
        String lastName,

        @NotBlank(message = "{validation.auth.email.notblank}")
        @Email(message = "{validation.auth.email.invalid}")
        String email,

        @NotBlank(message = "{validation.auth.username.notblank}")
        @Size(min = ValidationConstants.User.MIN_USERNAME_LENGTH, max = ValidationConstants.User.MAX_USERNAME_LENGTH, message = "{validation.auth.username.size}")
        String username,

        @NotBlank(message = "{validation.auth.password.notblank}")
        @Size(min = ValidationConstants.Password.MIN_LENGTH, message = "{validation.auth.password.size}")
        String password
) {
}
