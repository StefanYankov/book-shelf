package bg.softuni.bookshelf.service.genre.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for creating a new Genre.
 */
@Builder
public record GenreCreateDto(
        @NotBlank(message = "{validation.genre.name.notblank}")
        @Size(max = ValidationConstants.Genre.MAX_NAME_LENGTH, message = "{validation.genre.name.toolong}")
        String name,
        @Size(max = ValidationConstants.Genre.MAX_DESCRIPTION_LENGTH, message = "{validation.genre.summary.toolong}")
        String description
) {}
