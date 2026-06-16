package bg.softuni.bookshelf.service.author;

import bg.softuni.bookshelf.service.author.dto.AuthorCreateDto;
import bg.softuni.bookshelf.service.author.dto.AuthorDetailsDto;
import bg.softuni.bookshelf.service.author.dto.AuthorSummaryDto;
import bg.softuni.bookshelf.service.author.dto.AuthorUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for managing the Author aggregate root.
 * <p>
 * Defines the public contract for all business operations related to authors,
 * separating the API from the implementation details.
 */
public interface AuthorService {

    /**
     * Creates a new author and handles their profile image upload.
     * <p>
     * This method returns the created resource's DTO to optimize client-side workflows,
     * avoiding a subsequent fetch call.
     *
     * @param createDto The DTO containing the author's details.
     * @param imageFile The optional profile image file.
     * @return A detailed view DTO of the newly created author.
     */
    AuthorDetailsDto createAuthor(AuthorCreateDto createDto, MultipartFile imageFile);

    /**
     * Retrieves a single author by their unique ID.
     * <p>
     * The 'get' prefix implies a contract that an {@link AuthorDetailsDto} will be returned.
     * If the author is not found, a business-specific exception will be thrown.
     *
     * @param id The UUID of the author.
     * @return A detailed view DTO of the author, including their books.
     */
    AuthorDetailsDto getById(UUID id);

    /**
     * Retrieves a paginated list of all authors in a summary format.
     *
     * @param pageable The pagination information.
     * @return A page of author summary DTOs.
     */
    Page<AuthorSummaryDto> getAll(Pageable pageable);

    /**
     * Partially updates an existing author's information.
     * <p>
     * This method returns the full, updated state of the resource to prevent the client
     * from needing to make a subsequent fetch call.
     *
     * @param id The UUID of the author to update.
     * @param updateDto The DTO containing the fields to update.
     * @return A detailed view DTO of the author after the update has been applied.
     */
    AuthorDetailsDto updateAuthor(UUID id, AuthorUpdateDto updateDto);

    /**
     * Deletes an author from the system.
     *
     * @param id The UUID of the author to delete.
     */
    void deleteAuthor(UUID id);
}
