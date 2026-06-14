package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link ApplicationUser} entity.
 */
@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, UUID> {

}
