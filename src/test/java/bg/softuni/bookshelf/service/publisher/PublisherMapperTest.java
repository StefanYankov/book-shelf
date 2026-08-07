package bg.softuni.bookshelf.service.publisher;

import bg.softuni.bookshelf.data.entity.Publisher;
import bg.softuni.bookshelf.service.publisher.dto.PublisherCreateDto;
import bg.softuni.bookshelf.service.publisher.dto.PublisherDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PublisherMapper Unit Tests")
class PublisherMapperTest {

    private final PublisherMapper mapper = new PublisherMapper();

    @Test
    @DisplayName("toDto maps id and name")
    void toDto() {
        // Arrange
        UUID id = UUID.randomUUID();
        Publisher publisher = new Publisher();
        publisher.setId(id);
        publisher.setName("Penguin Books");

        // Act
        PublisherDto dto = mapper.toDto(publisher);

        // Assert
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Penguin Books");
    }

    @Test
    @DisplayName("toEntity maps the name")
    void toEntity() {
        // Arrange & Act
        Publisher result = mapper.toEntity(PublisherCreateDto.builder().name("Doubleday").build());

        // Assert
        assertThat(result.getName()).isEqualTo("Doubleday");
    }
}