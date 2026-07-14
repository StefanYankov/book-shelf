package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Review} entity.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Finds all reviews for a specific target entity, with pagination.
     *
     * @param targetId   The UUID of the target entity (e.g., a book).
     * @param targetType The type of the target entity (e.g., "BOOK").
     * @param pageable   The pagination information.
     * @return A page of reviews.
     */
    Page<Review> findAllByTargetIdAndTargetType(UUID targetId, String targetType, Pageable pageable);

    /**
     * Checks if a review exists for a specific user and target entity.
     *
     * @param userId     The UUID of the user.
     * @param targetId   The UUID of the target entity.
     * @param targetType The type of the target entity.
     * @return True if a review exists, false otherwise.
     */
    boolean existsByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType);
}
