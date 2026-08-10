package bg.softuni.bookshelf.service.publisher.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for partially updating an existing Publisher.
 * All fields are optional. Validation annotations are still applied
 * if a value is present.
 */
@Builder
public record PublisherUpdateDto(
        @Schema(description = "New publisher name; must be unique (case-insensitive).",
                example = "Penguin Classics")
        @Size(max = ValidationConstants.Publisher.MAX_NAME_LENGTH, message = "{validation.publisher.name.toolong}")
        String name
) {

}