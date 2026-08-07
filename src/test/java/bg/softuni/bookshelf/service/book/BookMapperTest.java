package bg.softuni.bookshelf.service.book;

import bg.softuni.bookshelf.data.entity.*;
import bg.softuni.bookshelf.data.entity.value.Image;
import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.service.book.dto.BookCreateDto;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.service.book.dto.GenreDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookMapper Unit Tests")
class BookMapperTest {

    private final BookMapper mapper = new BookMapper();

    private Author author(String name) {
        Author a = new Author();
        a.setId(UUID.randomUUID());
        a.setName(name);
        return a;
    }

    private Book fullBook(Image cover) {
        Language language = new Language();
        language.setId(UUID.randomUUID());
        language.setName("English");
        Publisher publisher = new Publisher();
        publisher.setId(UUID.randomUUID());
        publisher.setName("Chilton");
        Genre genre = new Genre();
        genre.setId(UUID.randomUUID());
        genre.setName("Sci-Fi");

        Book book = Book.builder()
                .title("Dune")
                .ISBN("9780441172719")
                .pages(412)
                .yearPublished(1965)
                .summary("Arrakis.")
                .format(BookFormat.PAPERBACK)
                .author(author("Frank Herbert"))
                .language(language)
                .publisher(publisher)
                .genres(Set.of(genre))
                .build();
        book.setId(UUID.randomUUID());
        book.setCoverImage(cover);
        return book;
    }

    @Test
    @DisplayName("toBookEntity maps all creation fields")
    void toEntity() {
        // Arrange
        BookCreateDto dto = BookCreateDto.builder()
                .title("Dune").isbn("9780441172719").pages(412).yearPublished(1965)
                .summary("Arrakis.").format(BookFormat.PAPERBACK)
                .authorId(UUID.randomUUID()).languageId(UUID.randomUUID())
                .publisherId(UUID.randomUUID()).genreIds(Set.of(UUID.randomUUID()))
                .build();
        Author author = author("Frank Herbert");
        Language language = new Language();
        Publisher publisher = new Publisher();
        Genre genre = new Genre();

        // Act
        Book book = mapper.toBookEntity(dto, author, language, publisher, Set.of(genre));

        // Assert
        assertThat(book.getTitle()).isEqualTo("Dune");
        assertThat(book.getISBN()).isEqualTo("9780441172719");
        assertThat(book.getPages()).isEqualTo(412);
        assertThat(book.getYearPublished()).isEqualTo(1965);
        assertThat(book.getFormat()).isEqualTo(BookFormat.PAPERBACK);
        assertThat(book.getAuthor()).isEqualTo(author);
        assertThat(book.getGenres()).containsExactly(genre);
    }

    @Test
    @DisplayName("toBookDetailsDto maps all fields, nested DTOs, and the cover URL")
    void toDetails_withCover() {
        // Arrange
        Book book = fullBook(Image.builder().url("https://cdn/dune.jpg").publicId("covers/dune").build());

        // Act
        BookDetailsDto dto = mapper.toBookDetailsDto(book);

        // Assert
        assertThat(dto.title()).isEqualTo("Dune");
        assertThat(dto.author().name()).isEqualTo("Frank Herbert");
        assertThat(dto.language().name()).isEqualTo("English");
        assertThat(dto.publisher().name()).isEqualTo("Chilton");
        assertThat(dto.genres()).extracting(GenreDto::name).containsExactly("Sci-Fi");
        assertThat(dto.coverImageUrl()).isEqualTo("https://cdn/dune.jpg");
    }

    @Test
    @DisplayName("toBookDetailsDto yields a null cover URL when there is no cover")
    void toDetails_withoutCover() {
        // Arrange & Act
        BookDetailsDto dto = mapper.toBookDetailsDto(fullBook(null));

        // Assert
        assertThat(dto.coverImageUrl()).isNull();
    }

    @Test
    @DisplayName("toBookSummaryDto maps id, title, author name, and cover URL")
    void toSummary_withCover() {
        // Arrange
        Book book = fullBook(Image.builder().url("https://cdn/dune.jpg").publicId("covers/dune").build());

        // Act
        BookSummaryDto dto = mapper.toBookSummaryDto(book);

        // Assert
        assertThat(dto.title()).isEqualTo("Dune");
        assertThat(dto.authorName()).isEqualTo("Frank Herbert");
        assertThat(dto.coverImageUrl()).isEqualTo("https://cdn/dune.jpg");
    }

    @Test
    @DisplayName("toBookSummaryDto yields a null cover URL when there is no cover")
    void toSummary_withoutCover() {
        // Arrange & Act
        BookSummaryDto dto = mapper.toBookSummaryDto(fullBook(null));

        // Assert
        assertThat(dto.coverImageUrl()).isNull();
    }
}