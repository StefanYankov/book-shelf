package bg.softuni.bookshelf.service.bookshelf;

import bg.softuni.bookshelf.data.entity.Bookshelf;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfSummaryDto;
import org.springframework.stereotype.Component;

/**
 * Component responsible for mapping between Bookshelf entities and their corresponding DTOs.
 * This separation of concerns keeps the service layer clean from mapping logic.
 */
@Component
public class BookshelfMapper {

    /**
     * Maps a {@link Bookshelf} entity to a {@link BookshelfSummaryDto} for list displays.
     *
     * @param bookshelf The persistent {@link Bookshelf} entity, or {@code null}.
     * @return A lightweight {@link BookshelfSummaryDto}, or {@code null} when the input is {@code null}.
     */
    public BookshelfSummaryDto toShelfSummaryDto(Bookshelf bookshelf) {
        if (bookshelf == null) {
            return null;
        }
        return BookshelfSummaryDto.builder()
                .id(bookshelf.getId())
                .name(bookshelf.getName())
                .bookCount(bookshelf.getBooks() != null ? bookshelf.getBooks().size() : 0)
                .build();
    }

    /**
     * Maps a {@link Bookshelf} entity to a {@link BookshelfDetailsDto}. The books page is left
     * null here and populated separately by the service to allow for pagination.
     *
     * @param bookshelf The persistent {@link Bookshelf} entity, or {@code null}.
     * @return A {@link BookshelfDetailsDto} without books, or {@code null} when the input is {@code null}.
     */
    public BookshelfDetailsDto toBookshelfDetailsDto(Bookshelf bookshelf) {
        if (bookshelf == null) {
            return null;
        }
        // The books themselves are handled separately by the service to allow for pagination.
        return BookshelfDetailsDto.builder()
                .id(bookshelf.getId())
                .name(bookshelf.getName())
                .description(bookshelf.getDescription())
                .books(null) // Books are loaded separately
                .build();
    }
}
