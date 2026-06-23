package bg.softuni.bookshelf.service.user.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateProfileDto(
        @NotBlank(message = "{user.firstName.notBlank}")
        @Size(
                min = ValidationConstants.User.MIN_FIRST_NAME_LENGTH,
                max = ValidationConstants.User.MAX_FIRST_NAME_LENGTH,
                message = "{user.firstName.size}"
        )
        String firstName,

        @NotBlank(message = "{user.lastName.notBlank}")
        @Size(
                min = ValidationConstants.User.MIN_LAST_NAME_LENGTH,
                max = ValidationConstants.User.MAX_LAST_NAME_LENGTH,
                message = "{user.lastName.size}"
        )
        String lastName
) {
}
