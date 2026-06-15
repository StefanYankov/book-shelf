package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {

    // developer comment: didn't go for enumeration, as if there are changes it requires the app to be re-compiled, but of course adds database complexity tradeoff
}
