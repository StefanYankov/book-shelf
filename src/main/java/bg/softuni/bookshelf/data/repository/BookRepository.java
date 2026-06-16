package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Book} entity.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

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
}
