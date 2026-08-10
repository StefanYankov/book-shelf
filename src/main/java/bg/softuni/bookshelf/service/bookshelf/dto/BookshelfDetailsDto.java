package bg.softuni.bookshelf.service.bookshelf.dto;

import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for returning a bookshelf with a page of its books.
 */
@Builder
public record BookshelfDetailsDto(
        @Schema(description = "Unique shelf identifier.",
                example = "bbbbbbbb-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Shelf name.", example = "Favourites")
        String name,

        @Schema(description = "Shelf description.",
                example = "Books I want to reread every year.")
        String description,

        @Schema(description = "A page of books contained in the shelf.")
        PagedResponse<BookSummaryDto> books
) {

}