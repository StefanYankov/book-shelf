package bg.softuni.bookshelf.service.author;

import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.data.entity.value.Image;
import bg.softuni.bookshelf.service.author.dto.AuthorCreateDto;
import bg.softuni.bookshelf.service.author.dto.AuthorDetailsDto;
import bg.softuni.bookshelf.service.author.dto.AuthorSummaryDto;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthorMapper Unit Tests")
class AuthorMapperTest {

    private final AuthorMapper mapper = new AuthorMapper();

    private Author author(UUID id, Image image) {
        Author author = new Author();
        author.setId(id);
        author.setName("Frank Herbert");
        author.setSummary("Sci-fi author.");
        author.setImage(image);
        return author;
    }

    @Test
    @DisplayName("toEntity maps name and summary")
    void toEntity() {
        // Arrange
        AuthorCreateDto dto = new AuthorCreateDto("Isaac Asimov", "Foundation author.");

        // Act
        Author result = mapper.toEntity(dto);

        // Assert
        assertThat(result.getName()).isEqualTo("Isaac Asimov");
        assertThat(result.getSummary()).isEqualTo("Foundation author.");
    }

    @Test
    @DisplayName("toDetailsDto maps all fields including the image URL and books page")
    void toDetailsDto_withImage() {
        // Arrange
        UUID id = UUID.randomUUID();
        Image image = Image.builder().url("https://cdn/a.jpg").publicId("authors/a").build();
        Page<BookSummaryDto> books = new PageImpl<>(List.of(new BookSummaryDto(UUID.randomUUID(), "Dune", "Herbert", null)));

        // Act
        AuthorDetailsDto dto = mapper.toDetailsDto(author(id, image), books);

        // Assert
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Frank Herbert");
        assertThat(dto.summary()).isEqualTo("Sci-fi author.");
        assertThat(dto.imageUrl()).isEqualTo("https://cdn/a.jpg");
        assertThat(dto.books()).isEqualTo(books);
    }

    @Test
    @DisplayName("toDetailsDto yields a null image URL when the author has no image")
    void toDetailsDto_withoutImage() {
        // Arrange
        Page<BookSummaryDto> books = new PageImpl<>(List.of());

        // Act
        AuthorDetailsDto dto = mapper.toDetailsDto(author(UUID.randomUUID(), null), books);

        // Assert
        assertThat(dto.imageUrl()).isNull();
    }

    @Test
    @DisplayName("toSummaryDto maps id, name, and image URL")
    void toSummaryDto_withImage() {
        // Arrange
        UUID id = UUID.randomUUID();
        Image image = Image.builder().url("https://cdn/a.jpg").publicId("authors/a").build();

        // Act
        AuthorSummaryDto dto = mapper.toSummaryDto(author(id, image));

        // Assert
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Frank Herbert");
        assertThat(dto.imageUrl()).isEqualTo("https://cdn/a.jpg");
    }

    @Test
    @DisplayName("toSummaryDto yields a null image URL when the author has no image")
    void toSummaryDto_withoutImage() {
        // Arrange & Act
        AuthorSummaryDto dto = mapper.toSummaryDto(author(UUID.randomUUID(), null));

        // Assert
        assertThat(dto.imageUrl()).isNull();
    }
}