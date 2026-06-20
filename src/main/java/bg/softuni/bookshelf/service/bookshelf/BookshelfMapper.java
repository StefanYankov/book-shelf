package bg.softuni.bookshelf.service.bookshelf;

import bg.softuni.bookshelf.data.entity.Bookshelf;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class BookshelfMapper {

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
