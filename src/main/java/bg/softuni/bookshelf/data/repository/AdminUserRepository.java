package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.identity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link AdminUser} entity.
 */
@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {

}
