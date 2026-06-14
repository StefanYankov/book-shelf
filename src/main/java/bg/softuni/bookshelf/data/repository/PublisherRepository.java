package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Publisher} entities.
 */
@Repository
public interface PublisherRepository extends JpaRepository<Publisher, UUID> {

}
