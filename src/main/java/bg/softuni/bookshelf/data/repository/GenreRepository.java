package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Genre} entities.
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {

    /**
     * Finds a genre by its name, ignoring case.
     *
     * @param name The name of the genre to find.
     * @return An Optional containing the found genre or empty if not found.
     */
    Optional<Genre> findByNameIgnoreCase(String name);
}
