package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.BookshelfBook;
import bg.softuni.bookshelf.data.entity.BookshelfBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link BookshelfBook} join entity.
 * <p>
 * This interface manages the relationship between a {@link bg.softuni.bookshelf.data.entity.Bookshelf}
 * and a {@link bg.softuni.bookshelf.data.entity.Book}. The primary key is the composite
 * {@link BookshelfBookId}.
 */
@Repository
public interface BookshelfBookRepository extends JpaRepository<BookshelfBook, BookshelfBookId> {
}
