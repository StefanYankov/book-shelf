package bg.softuni.bookshelf.service.genre.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for partially updating an existing Genre.
 * All fields are optional. Validation annotations are still applied
 * if a value is present.
 */
@Builder
public record GenreUpdateDto(
        @Schema(description = "New genre name; must be unique (case-insensitive).",
                example = "Hard Science Fiction")
        @Size(max = ValidationConstants.Genre.MAX_NAME_LENGTH, message = "{validation.genre.name.toolong}")
        String name,

        @Schema(description = "New description of the genre.",
                example = "Science fiction that emphasizes scientific accuracy and technical detail.")
        @Size(max = ValidationConstants.Genre.MAX_DESCRIPTION_LENGTH, message = "{validation.genre.summary.toolong}")
        String description
) {}