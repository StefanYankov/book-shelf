package bg.softuni.bookshelf.service.author.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new Author.
 */
public record AuthorCreateDto(
        @NotBlank(message = "{validation.author.name.notblank}")
        @Size(max = ValidationConstants.Author.MAX_NAME_LENGTH, message = "{validation.author.name.toolong}")
        String name,

        @Size(max = ValidationConstants.Author.MAX_SUMMARY_LENGTH, message = "{validation.author.summary.toolong}")
        String summary
) {
}
