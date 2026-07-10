package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.data.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Book} entity.
 * Extends JpaSpecificationExecutor to process composite runtime criteria query predicates.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {

    /**
     * Fetches a paginated list of books from the database and eagerly loads their associated
     * {@link bg.softuni.bookshelf.data.entity.Author} in a single, optimized query.
     * <p>
     * This method is designed to solve the N+1 query problem that would otherwise
     * occur when accessing the author of each book in a list.
     *
     * @param pageable The pagination information.
     * @return a page of books with their authors fully initialized.
     */
    @Query(value = "SELECT b FROM Book b JOIN FETCH b.author",
           countQuery = "SELECT COUNT(b) FROM Book b")
    Page<Book> findAllWithAuthors(Pageable pageable);

    /**
     * Finds a paginated list of all books written by a specific author.
     *
     * @param authorId The UUID of the author.
     * @param pageable The pagination information.
     * @return A page of books by the specified author.
     */
    Page<Book> findAllByAuthorId(UUID authorId, Pageable pageable);

    /**
     * Performs a case-insensitive search for books by title or author name.
     * <p>
     * This method uses a {@code JOIN FETCH} to eagerly load the {@link Author} and a
     * {@code LEFT JOIN} for the filtering criteria, ensuring that the query is efficient
     * and solves the N+1 problem.
     *
     * @param query    The search string (partial match) to look for in title or author name.
     * @param pageable The pagination information to restrict the result size.
     * @return A {@link Page} of {@link Book} entities matching the criteria.
     */
    @Query("SELECT b FROM Book b JOIN FETCH b.author a " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> searchByTitleOrAuthor(@Param("query") String query, Pageable pageable);

    /**
     * Fetches a single book by its ID, eagerly loading its core relationships (author, publisher, language)
     * and its collection of genres to prevent N+1 queries when building a detailed view.
     *
     * @param id The UUID of the book.
     * @return An Optional containing the fully initialized Book entity, or empty if not found.
     */
    @Query("SELECT b FROM Book b " +
            "JOIN FETCH b.author " +
            "JOIN FETCH b.publisher " +
            "JOIN FETCH b.language " +
            "LEFT JOIN FETCH b.genres " +
            "WHERE b.id = :id")
    Optional<Book> findBookDetailsById(@Param("id") UUID id);
}
