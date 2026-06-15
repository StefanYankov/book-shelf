package bg.softuni.bookshelf.service.language.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new Language.
 */
public record LanguageCreateDto(
        @NotBlank(message = "{validation.language.name.notblank}")
        @Size(max = ValidationConstants.Language.MAX_NAME_LENGTH, message = "{validation.language.name.toolong}")
        String name
) {
}
