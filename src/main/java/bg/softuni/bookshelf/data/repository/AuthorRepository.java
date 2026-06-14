package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


/**
 * Spring Data JPA repository for the {@link Author} entities.
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {

}
