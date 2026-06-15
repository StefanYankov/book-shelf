package bg.softuni.bookshelf.service.book;

import bg.softuni.bookshelf.service.book.dto.BookCreateDto;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.service.book.dto.BookUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

/**
 * Service interface for managing the Book aggregate root.
 * <p>
 * Defines the public contract for all business operations related to books,
 * separating the API from the implementation details.
 */
public interface BookService {

    /**
     * Creates a new book and handles the cover image upload.
     * <p>
     * This method acts as a Command (changing state) but pragmatically returns a {@link BookDetailsDto}
     * to optimize client-side workflows. This avoids an additional API call for the client
     * to fetch the newly created resource, enhancing user experience and reducing latency.
     *
     * @param createDto The DTO containing the book's details.
     * @param coverImageFile The optional cover image file.
     * @return A detailed view DTO of the newly created book.
     */
    BookDetailsDto createBook(BookCreateDto createDto, MultipartFile coverImageFile);

    /**
     * Retrieves a single book by its unique ID.
     * <p>
     * The 'get' prefix implies a contract that a {@link BookDetailsDto} will be returned.
     * If the book is not found, a business-specific exception (e.g., BookNotFoundException)
     * will be thrown, which should be handled by a global exception handler.
     *
     * @param id The UUID of the book.
     * @return A detailed view DTO of the book.
     */
    BookDetailsDto getById(UUID id);

    /**
     * Retrieves a paginated list of all books in a summary format.
     *
     * @param pageable The pagination information.
     * @return A page of book summary DTOs.
     */
    Page<BookSummaryDto> getAll(Pageable pageable);

    /**
     * Partially updates an existing book's information.
     * <p>
     * This method returns the full, updated state of the resource to prevent the client
     * from needing to make a subsequent fetch call to see the result of the update.
     *
     * @param id The UUID of the book to update.
     * @param updateDto The DTO containing the fields to update.
     * @return A detailed view DTO of the book after the update has been applied.
     */
    BookDetailsDto updateBook(UUID id, BookUpdateDto updateDto);

    /**
     * Deletes a book from the system.
     *
     * @param id The UUID of the book to delete.
     */
    void deleteBook(UUID id);
}
