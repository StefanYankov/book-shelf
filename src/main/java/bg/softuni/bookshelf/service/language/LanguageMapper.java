package bg.softuni.bookshelf.service.language;

import bg.softuni.bookshelf.data.entity.Language;
import bg.softuni.bookshelf.service.language.dto.LanguageCreateDto;
import bg.softuni.bookshelf.service.language.dto.LanguageDto;
import org.springframework.stereotype.Component;

/**
 * Component responsible for mapping between Language entities and their corresponding DTOs.
 */
@Component
public class LanguageMapper {

    /**
     * Maps a {@link Language} entity to a {@link LanguageDto}.
     *
     * @param language The persistent {@link Language} entity.
     * @return A {@link LanguageDto}.
     */
    public LanguageDto toDto(Language language) {
        return new LanguageDto(language.getId(), language.getName());
    }

    /**
     * Maps a {@link LanguageCreateDto} to a new {@link Language} entity.
     *
     * @param dto The source DTO containing the language's creation data.
     * @return A new, transient {@link Language} entity ready for persistence.
     */
    public Language toEntity(LanguageCreateDto dto) {
        Language language = new Language();
        language.setName(dto.name());
        return language;
    }
}
