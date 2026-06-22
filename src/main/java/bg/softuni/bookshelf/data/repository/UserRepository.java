package bg.softuni.bookshelf.data.repository;
import bg.softuni.bookshelf.data.entity.identity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link User} entities.
 * This is the central data access point for the IAM (Identity & Access Management) domain.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their exact username.
     * This is a critical query for the Spring Security authentication process (login).
     *
     * @param username the unique username to search for
     * @return an Optional containing the User if found, or empty if not
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their exact email address.
     * Useful for registration validation and password reset flows.
     *
     * @param email the unique email to search for
     * @return an Optional containing the User if found, or empty if not
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their unique ID.
     * While provided by JpaRepository, explicitly declaring it makes the repository's
     * capabilities clearer and aids in testing and mocking.
     *
     * @param id the UUID of the user
     * @return an Optional containing the User if found, or empty if not
     */
    Optional<User> findById(UUID id);
}
