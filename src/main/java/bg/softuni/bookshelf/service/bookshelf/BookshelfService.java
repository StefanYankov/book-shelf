package bg.softuni.bookshelf.service.bookshelf;

import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.service.bookshelf.dto.*;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing a user's personal bookshelves.
 * <p>
 * This contract defines all business operations related to creating, managing,
 * and viewing custom bookshelves and their contents.
 */
public interface BookshelfService {

    /**
     * Creates a new, empty bookshelf for a specific user.
     * <p>
     * This method follows the "write-through-get" pattern. It returns the full
     * state of the newly created resource to prevent the client from needing to
     * make a subsequent API call, which improves performance and simplifies client-side logic.
     *
     * @param createDto The DTO containing the new shelf's details.
     * @param ownerId   The UUID of the user who will own this shelf.
     * @return A {@link BookshelfDetailsDto} representing the newly created bookshelf.
     */
    BookshelfDetailsDto createShelf(BookshelfCreateDto createDto, UUID ownerId);

    /**
     * Retrieves a paginated list of all bookshelves owned by a specific user.
     *
     * @param userId   The UUID of the user.
     * @param pageable The pagination and sorting information.
     * @return A {@link PagedResponse} of {@link BookshelfSummaryDto} objects.
     */
    PagedResponse<BookshelfSummaryDto> getShelvesForUser(UUID userId, Pageable pageable);

    /**
     * Retrieves the detailed view of a single bookshelf, including a paginated list of its books.
     *
     * @param shelfId The UUID of the bookshelf.
     * @return A {@link BookshelfDetailsDto} containing the shelf's details and its books.
     */
    BookshelfDetailsDto getShelfById(UUID shelfId);

    /**
     * Retrieves a paginated list of all books contained within a specific bookshelf.
     *
     * @param shelfId  The UUID of the bookshelf.
     * @param pageable The pagination and sorting information for the books.
     * @return A {@link PagedResponse} of {@link BookSummaryDto} objects.
     */
    PagedResponse<BookSummaryDto> getBooksInShelf(UUID shelfId, Pageable pageable);

    /**
     * Updates the details (e.g., name, description) of an existing bookshelf.
     *
     * @param shelfId   The UUID of the bookshelf to update.
     * @param updateDto The DTO containing the new details.
     * @return A {@link BookshelfDetailsDto} representing the updated state of the shelf.
     */
    BookshelfDetailsDto updateShelf(UUID shelfId, BookshelfUpdateDto updateDto);

    /**
     * Deletes a bookshelf. This action is permanent and will also remove all
     * associations between this shelf and any books it contained.
     *
     * @param shelfId The UUID of the bookshelf to delete.
     */
    void deleteShelf(UUID shelfId);

    /**
     * Adds a book to a specific bookshelf.
     *
     * @param shelfId    The UUID of the bookshelf.
     * @param addBookDto The DTO containing the UUID of the book to add.
     */
    void addBookToShelf(UUID shelfId, AddBookToBookshelfDto addBookDto);

    /**
     * Removes a book from a specific bookshelf.
     *
     * @param shelfId The UUID of the bookshelf.
     * @param bookId  The UUID of the book to remove.
     */
    void removeBookFromShelf(UUID shelfId, UUID bookId);
}