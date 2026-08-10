package bg.softuni.bookshelf.service.bookshelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for adding a book to a bookshelf.
 */
@Builder
public record AddBookToBookshelfDto(
        @Schema(description = "Identifier of the book to add to the shelf.",
                example = "66666666-0000-0000-0000-000000000001")
        @NotNull(message = "{validation.bookshelf.bookId.notnull}")
        UUID bookId
) {
}