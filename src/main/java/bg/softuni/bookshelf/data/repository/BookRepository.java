package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Book} entity.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    /**
     * Fetches all books from the database and eagerly loads their associated
     * {@link bg.softuni.bookshelf.data.entity.Author} in a single, optimized query.
     *
     * @return a list of all books with their authors fully initialized.
     */
    @Query("SELECT b FROM Book b JOIN FETCH b.author")
    List<Book> findAllWithAuthors();
}
