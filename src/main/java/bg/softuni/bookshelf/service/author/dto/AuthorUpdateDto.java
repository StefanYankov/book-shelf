package bg.softuni.bookshelf.service.author.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for partially updating an existing Author.
 * All fields are optional. Validation annotations are still applied
 * if a value is present.
 */
@Builder
public record AuthorUpdateDto(
        @Size(max = ValidationConstants.Author.MAX_NAME_LENGTH, message = "{validation.author.name.toolong}")
        String name,

        @Size(max = ValidationConstants.Author.MAX_SUMMARY_LENGTH, message = "{validation.author.summary.toolong}")
        String summary
) {
}
