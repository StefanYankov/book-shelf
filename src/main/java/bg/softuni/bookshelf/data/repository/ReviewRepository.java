package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Review} entities.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

}
