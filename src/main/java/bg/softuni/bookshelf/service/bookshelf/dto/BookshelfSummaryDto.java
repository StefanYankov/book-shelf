package bg.softuni.bookshelf.service.bookshelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for returning a bookshelf summary, suitable for lists.
 */
@Builder
public record BookshelfSummaryDto(
        @Schema(description = "Unique shelf identifier.",
                example = "bbbbbbbb-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Shelf name.", example = "Favourites")
        String name,

        @Schema(description = "Number of books currently on the shelf.", example = "12")
        int bookCount
) {

}