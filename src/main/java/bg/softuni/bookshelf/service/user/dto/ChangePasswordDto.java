package bg.softuni.bookshelf.service.user.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChangePasswordDto(
        @NotBlank(message = "{user.password.notBlank}")
        String currentPassword,

        @NotBlank(message = "{user.password.notBlank}")
        @Size(
                min = ValidationConstants.User.MIN_PASSWORD_LENGTH,
                max = ValidationConstants.User.MAX_PASSWORD_LENGTH,
                message = "{user.password.size}"
        )
        String newPassword
) {
}
