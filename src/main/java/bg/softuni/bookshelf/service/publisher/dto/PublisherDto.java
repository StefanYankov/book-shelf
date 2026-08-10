package bg.softuni.bookshelf.service.publisher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO for returning Publisher information.
 */
public record PublisherDto(
        @Schema(description = "Unique publisher identifier.",
                example = "99999999-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Publisher name.", example = "Penguin Books")
        String name
) {

}