package bg.softuni.bookshelf.service.bookshelf;

import bg.softuni.bookshelf.data.entity.Bookshelf;
import bg.softuni.bookshelf.data.entity.BookshelfBook;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfSummaryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookshelfMapper Unit Tests")
class BookshelfMapperTest {

    private final BookshelfMapper mapper = new BookshelfMapper();

    private Bookshelf shelf(UUID id, List<BookshelfBook> books) {
        Bookshelf shelf = new Bookshelf();
        shelf.setId(id);
        shelf.setName("Favorites");
        shelf.setDescription("My favorites.");
        if (books != null) {
            shelf.setBooks(books);
        }
        return shelf;
    }

    @Test
    @DisplayName("toShelfSummaryDto maps fields and counts the books")
    void toSummary_withBooks() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        BookshelfSummaryDto dto = mapper.toShelfSummaryDto(shelf(id, List.of(new BookshelfBook(), new BookshelfBook())));

        // Assert
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Favorites");
        assertThat(dto.bookCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("toShelfSummaryDto returns null for a null shelf")
    void toSummary_null() {
        // Arrange & Act & Assert
        assertThat(mapper.toShelfSummaryDto(null)).isNull();
    }

    @Test
    @DisplayName("toBookshelfDetailsDto maps identity fields and leaves books null (loaded separately)")
    void toDetails_maps() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        BookshelfDetailsDto dto = mapper.toBookshelfDetailsDto(shelf(id, null));

        // Assert
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Favorites");
        assertThat(dto.description()).isEqualTo("My favorites.");
        assertThat(dto.books()).isNull();
    }

    @Test
    @DisplayName("toBookshelfDetailsDto returns null for a null shelf")
    void toDetails_null() {
        // Arrange & Act & Assert
        assertThat(mapper.toBookshelfDetailsDto(null)).isNull();
    }
}