package bg.softuni.bookshelf.service.auth.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Defines the API contract for completing a password reset.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "{validation.auth.token.notblank}")
        String token,

        @NotBlank(message = "{validation.auth.password.notblank}")
        @Size(min = ValidationConstants.Password.MIN_LENGTH, message = "{validation.auth.password.size}")
        String newPassword
) {
}
