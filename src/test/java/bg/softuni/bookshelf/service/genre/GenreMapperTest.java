package bg.softuni.bookshelf.service.genre;

import bg.softuni.bookshelf.data.entity.Genre;
import bg.softuni.bookshelf.service.genre.dto.GenreCreateDto;
import bg.softuni.bookshelf.service.genre.dto.GenreDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenreMapper Unit Tests")
class GenreMapperTest {

    private final GenreMapper mapper = new GenreMapper();

    @Test
    @DisplayName("toDto maps id, name, and description")
    void toDto() {
        // Arrange
        UUID id = UUID.randomUUID();
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName("Science Fiction");
        genre.setDescription("Futuristic concepts.");

        // Act
        GenreDto dto = mapper.toDto(genre);

        // Assert
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Science Fiction");
        assertThat(dto.description()).isEqualTo("Futuristic concepts.");
    }

    @Test
    @DisplayName("toEntity maps the name")
    void toEntity() {
        // Arrange
        GenreCreateDto dto = GenreCreateDto.builder().name("Fantasy").description("Magic.").build();

        // Act
        Genre result = mapper.toEntity(dto);

        // Assert
        assertThat(result.getName()).isEqualTo("Fantasy");
    }
}