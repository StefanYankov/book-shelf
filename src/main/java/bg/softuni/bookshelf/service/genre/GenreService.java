package bg.softuni.bookshelf.service.genre;

import bg.softuni.bookshelf.service.genre.dto.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing the Genre entity.
 * <p>
 * Defines the public contract for all business operations related to languages.
 */
public interface GenreService {

    /**
     * Creates a new genre.
     *
     * @param createDto The DTO containing the genre's details.
     * @return A DTO of the newly created genre.
     */
    GenreDto createGenre(GenreCreateDto createDto);

    /**
     * Retrieves a single genre by its unique ID.
     *
     * @param id The UUID of the genre.
     * @return A DTO of the genre.
     */
    GenreDto getById(UUID id);

    /**
     * Retrieves a paginated list of all languages.
     *
     * @param pageable The pagination information.
     * @return A page of genre DTOs.
     */
    Page<GenreDto> getAll(Pageable pageable);

    /**
     * Partially updates an existing genre's information.
     *
     * @param id The UUID of the genre to update.
     * @param updateDto The DTO containing the fields to update.
     * @return A DTO of the genre after the update has been applied.
     */
    GenreDto updateGenre(UUID id, GenreUpdateDto updateDto);

    /**
     * Deletes a genre from the system.
     *
     * @param id The UUID of the genre to delete.
     */
    void deleteGenre(UUID id);
}
