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
        @Size(min = ValidationConstants.User.MIN_PASSWORD_LENGTH, max = ValidationConstants.User.MAX_PASSWORD_LENGTH, message = "{user.password.size}")
        String newPassword
) {
}
