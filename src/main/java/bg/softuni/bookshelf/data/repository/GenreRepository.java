package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Genre} entities.
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {

}
