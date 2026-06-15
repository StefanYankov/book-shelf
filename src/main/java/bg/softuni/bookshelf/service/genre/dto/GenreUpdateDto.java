package bg.softuni.bookshelf.service.genre.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Size;
import lombok.Builder;


/**
 * DTO for partially updating an existing Genre.
 * All fields are optional. Validation annotations are still applied
 * if a value is present.
 */
@Builder
public record GenreUpdateDto(
        @Size(max = ValidationConstants.Genre.MAX_NAME_LENGTH, message = "{validation.genre.name.toolong}")
        String name,
        @Size(max = ValidationConstants.Genre.MAX_DESCRIPTION_LENGTH, message = "{validation.genre.summary.toolong}")
        String description
) {}
