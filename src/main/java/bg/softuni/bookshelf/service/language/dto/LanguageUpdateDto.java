package bg.softuni.bookshelf.service.language.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for partially updating an existing Language.
 */
@Builder
public record LanguageUpdateDto(
        @Schema(description = "New language name; must be unique (case-insensitive).",
                example = "Bulgarian")
        @Size(max = ValidationConstants.Language.MAX_NAME_LENGTH, message = "{validation.language.name.toolong}")
        String name
) {
}