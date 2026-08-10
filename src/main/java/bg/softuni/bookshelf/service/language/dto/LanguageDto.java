package bg.softuni.bookshelf.service.language.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO for returning Language information.
 */
public record LanguageDto(
        @Schema(description = "Unique language identifier.",
                example = "88888888-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Language name.", example = "English")
        String name
) {
}