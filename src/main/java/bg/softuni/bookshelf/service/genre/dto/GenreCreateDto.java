package bg.softuni.bookshelf.service.genre.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for creating a new Genre.
 */
@Builder
public record GenreCreateDto(
        @Schema(description = "Genre name; must be unique (case-insensitive).",
                example = "Science Fiction")
        @NotBlank(message = "{validation.genre.name.notblank}")
        @Size(max = ValidationConstants.Genre.MAX_NAME_LENGTH, message = "{validation.genre.name.toolong}")
        String name,

        @Schema(description = "Short description of the genre.",
                example = "Fiction dealing with futuristic concepts such as advanced science and technology.")
        @Size(max = ValidationConstants.Genre.MAX_DESCRIPTION_LENGTH, message = "{validation.genre.summary.toolong}")
        String description
) {}