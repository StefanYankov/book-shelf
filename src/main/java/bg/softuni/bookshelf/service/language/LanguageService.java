package bg.softuni.bookshelf.service.language;

import bg.softuni.bookshelf.service.language.dto.LanguageCreateDto;
import bg.softuni.bookshelf.service.language.dto.LanguageDto;
import bg.softuni.bookshelf.service.language.dto.LanguageUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing the Language entity.
 * <p>
 * Defines the public contract for all business operations related to languages.
 */
public interface LanguageService {

    /**
     * Creates a new language.
     *
     * @param createDto The DTO containing the language's details.
     * @return A DTO of the newly created language.
     */
    LanguageDto createLanguage(LanguageCreateDto createDto);

    /**
     * Retrieves a single language by its unique ID.
     *
     * @param id The UUID of the language.
     * @return A DTO of the language.
     */
    LanguageDto getById(UUID id);

    /**
     * Retrieves a paginated list of all languages.
     *
     * @param pageable The pagination information.
     * @return A page of language DTOs.
     */
    Page<LanguageDto> getAll(Pageable pageable);

    /**
     * Partially updates an existing language's information.
     *
     * @param id The UUID of the language to update.
     * @param updateDto The DTO containing the fields to update.
     * @return A DTO of the language after the update has been applied.
     */
    LanguageDto updateLanguage(UUID id, LanguageUpdateDto updateDto);

    /**
     * Deletes a language from the system.
     *
     * @param id The UUID of the language to delete.
     */
    void deleteLanguage(UUID id);
}
