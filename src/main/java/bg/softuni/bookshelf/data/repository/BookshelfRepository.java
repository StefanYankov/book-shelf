package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Bookshelf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Bookshelf} entity.
 * <p>
 * This interface provides the standard CRUD operations for Bookshelf entities
 * and allows for the definition of custom query methods.
 */
@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, UUID> {

    /**
     * Finds all bookshelves owned by a specific user, with pagination and sorting.
     * <p>
     * This is a derived query method. Spring Data automatically implements the query
     * based on the method name. It translates to a JPQL query similar to:
     * "SELECT b FROM Bookshelf b WHERE b.user.id = :userId"
     *
     * @param userId   The UUID of the user who owns the bookshelves.
     * @param pageable The pagination and sorting information.
     * @return A {@link Page} of {@link Bookshelf} entities owned by the specified user.
     */
    Page<Bookshelf> findAllByUser_Id(UUID userId, Pageable pageable);
}
