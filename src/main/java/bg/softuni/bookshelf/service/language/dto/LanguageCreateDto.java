package bg.softuni.bookshelf.service.language.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new Language.
 */
public record LanguageCreateDto(
        @Schema(description = "Language name; must be unique (case-insensitive).",
                example = "English")
        @NotBlank(message = "{validation.language.name.notblank}")
        @Size(max = ValidationConstants.Language.MAX_NAME_LENGTH, message = "{validation.language.name.toolong}")
        String name
) {
}