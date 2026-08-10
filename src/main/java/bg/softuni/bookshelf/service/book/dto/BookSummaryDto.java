package bg.softuni.bookshelf.service.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for returning a summary of a book, suitable for lists.
 */
@Builder
public record BookSummaryDto(
        @Schema(description = "Unique book identifier.",
                example = "66666666-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Book title.", example = "Dune")
        String title,

        @Schema(description = "Primary author's display name.", example = "Frank Herbert")
        String authorName,

        @Schema(description = "URL of the cover image; empty when none is set.",
                example = "https://res.cloudinary.com/demo/image/upload/dune.jpg")
        String coverImageUrl
) {
}
