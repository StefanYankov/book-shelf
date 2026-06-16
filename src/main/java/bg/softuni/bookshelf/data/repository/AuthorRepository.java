package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


/**
 * Spring Data JPA repository for the {@link Author} entities.
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {

    /**
     * Finds an author by their name, ignoring case.
     *
     * @param name The name of the author to find.
     * @return An Optional containing the found author or empty if not found.
     */
    Optional<Author> findByNameIgnoreCase(String name);
}
