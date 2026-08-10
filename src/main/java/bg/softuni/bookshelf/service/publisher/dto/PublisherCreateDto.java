package bg.softuni.bookshelf.service.publisher.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for creating a new Publisher.
 */
@Builder
public record PublisherCreateDto(
        @Schema(description = "Publisher name; must be unique (case-insensitive).",
                example = "Penguin Books")
        @NotBlank(message = "{validation.publisher.name.notblank}")
        @Size(max = ValidationConstants.Publisher.MAX_NAME_LENGTH, message = "{validation.publisher.name.toolong}")
        String name
) {}