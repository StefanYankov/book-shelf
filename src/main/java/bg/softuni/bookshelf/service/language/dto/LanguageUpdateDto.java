package bg.softuni.bookshelf.service.language.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for partially updating an existing Language.
 */
@Builder
public record LanguageUpdateDto(
        @Size(max = ValidationConstants.Language.MAX_NAME_LENGTH, message = "{validation.language.name.toolong}")
        String name
) {
}
