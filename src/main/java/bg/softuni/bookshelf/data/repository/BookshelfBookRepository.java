package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Book;
import bg.softuni.bookshelf.data.entity.BookshelfBook;
import bg.softuni.bookshelf.data.entity.BookshelfBookId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link BookshelfBook} join entity.
 * <p>
 * This interface manages the relationship between a {@link bg.softuni.bookshelf.data.entity.Bookshelf}
 * and a {@link Book}. The primary key is the composite
 * {@link BookshelfBookId}.
 */
@Repository
public interface BookshelfBookRepository extends JpaRepository<BookshelfBook, BookshelfBookId> {

    /**
     * Finds all books contained within a specific bookshelf, with pagination and sorting.
     * <p>
     * This query uses a {@code JOIN FETCH} to eagerly load the associated {@link Book#getAuthor()}
     * for each book, preventing N+1 query problems. It also delegates pagination and sorting
     * to the database, which is highly efficient.
     *
     * @param shelfId  The UUID of the bookshelf.
     * @param pageable The pagination and sorting information.
     * @return A {@link Page} of {@link Book} entities with their authors initialized.
     */
    @Query(value = "SELECT bb.book FROM BookshelfBook bb JOIN FETCH bb.book.author WHERE bb.bookshelf.id = :shelfId",
           countQuery = "SELECT COUNT(bb) FROM BookshelfBook bb WHERE bb.bookshelf.id = :shelfId")
    Page<Book> findBooksByBookshelfId(@Param("shelfId") UUID shelfId, Pageable pageable);
}
