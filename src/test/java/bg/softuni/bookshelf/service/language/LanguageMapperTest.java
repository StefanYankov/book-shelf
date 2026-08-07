package bg.softuni.bookshelf.service.language;

import bg.softuni.bookshelf.data.entity.Language;
import bg.softuni.bookshelf.service.language.dto.LanguageCreateDto;
import bg.softuni.bookshelf.service.language.dto.LanguageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LanguageMapper Unit Tests")
class LanguageMapperTest {

    private final LanguageMapper mapper = new LanguageMapper();

    @Test
    @DisplayName("toDto maps id and name")
    void toDto() {
        // Arrange
        UUID id = UUID.randomUUID();
        Language language = new Language();
        language.setId(id);
        language.setName("English");

        // Act
        LanguageDto dto = mapper.toDto(language);

        // Assert
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("English");
    }

    @Test
    @DisplayName("toEntity maps the name")
    void toEntity() {
        // Arrange & Act
        Language result = mapper.toEntity(new LanguageCreateDto("Bulgarian"));

        // Assert
        assertThat(result.getName()).isEqualTo("Bulgarian");
    }
}