package bg.softuni.bookshelf.service.bookshelf.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for updating an existing bookshelf.
 */
@Builder
public record BookshelfUpdateDto(
        @Schema(description = "New shelf name.", example = "All-time Favourites")
        @NotBlank(message = "{validation.bookshelf.name.notblank}")
        @Size(
                min = ValidationConstants.Bookshelf.MIN_NAME_LENGTH,
                max = ValidationConstants.Bookshelf.MAX_NAME_LENGTH,
                message = "{validation.bookshelf.name.size}"
        )
        String name,

        @Schema(description = "New shelf description.",
                example = "The books that shaped how I read.")
        @Size(
                max = ValidationConstants.Bookshelf.MAX_DESCRIPTION_LENGTH,
                message = "{validation.bookshelf.description.size}"
        )
        String description
) {
}
