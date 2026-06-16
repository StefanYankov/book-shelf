package bg.softuni.bookshelf.service.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Defines the API contract for initiating a password reset.
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "{validation.auth.email.notblank}")
        @Email(message = "{validation.auth.email.invalid}")
        String email
) {
}
