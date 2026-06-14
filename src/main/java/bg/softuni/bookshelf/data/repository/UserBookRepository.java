package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link UserBook} entities.
 */
@Repository
public interface UserBookRepository extends JpaRepository<UserBook, UUID> {

}
