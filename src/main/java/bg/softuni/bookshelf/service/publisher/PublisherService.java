package bg.softuni.bookshelf.service.publisher;

import bg.softuni.bookshelf.service.publisher.dto.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing the Publisher entity.
 * <p>
 * Defines the public contract for all business operations related to languages.
 */
public interface PublisherService {

    /**
     * Creates a new publisher.
     *
     * @param createDto The DTO containing the publisher's details.
     * @return A DTO of the newly created publisher.
     */
    PublisherDto createPublisher(PublisherCreateDto createDto);

    /**
     * Retrieves a single publisher by its unique ID.
     *
     * @param id The UUID of the publisher.
     * @return A DTO of the publisher.
     */
    PublisherDto getById(UUID id);

    /**
     * Retrieves a paginated list of all languages.
     *
     * @param pageable The pagination information.
     * @return A page of publisher DTOs.
     */
    Page<PublisherDto> getAll(Pageable pageable);

    /**
     * Partially updates an existing publisher's information.
     *
     * @param id The UUID of the publisher to update.
     * @param updateDto The DTO containing the fields to update.
     * @return A DTO of the publisher after the update has been applied.
     */
    PublisherDto updatePublisher(UUID id, PublisherUpdateDto updateDto);

    /**
     * Deletes a publisher from the system.
     *
     * @param id The UUID of the publisher to delete.
     */
    void deletePublisher(UUID id);
}
