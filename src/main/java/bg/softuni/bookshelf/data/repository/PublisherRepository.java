package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Publisher} entities.
 */
@Repository
public interface PublisherRepository extends JpaRepository<Publisher, UUID> {

    /**
     * Finds a genre by its name, ignoring case.
     *
     * @param name The name of the publisher to find.
     * @return An Optional containing the found publisher or empty if not found.
     */
    Optional<Publisher> findByNameIgnoreCase(String name);
}
