package bg.softuni.bookshelf.service.genre.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO for returning a genre.
 */
public record GenreDto(
        @Schema(description = "Unique genre identifier.",
                example = "77777777-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Genre name.", example = "Science Fiction")
        String name,

        @Schema(description = "Short description of the genre.",
                example = "Fiction dealing with futuristic concepts such as advanced science and technology.")
        String description
) {}