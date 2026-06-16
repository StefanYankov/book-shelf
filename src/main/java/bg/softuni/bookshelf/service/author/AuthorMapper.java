package bg.softuni.bookshelf.service.author;

import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.service.author.dto.AuthorCreateDto;
import bg.softuni.bookshelf.service.author.dto.AuthorDetailsDto;
import bg.softuni.bookshelf.service.author.dto.AuthorSummaryDto;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Component responsible for mapping between Author entities and their corresponding DTOs.
 * This separation of concerns keeps the service layer clean from mapping logic.
 */
@Component
public class AuthorMapper {

    /**
     * Maps an {@link AuthorCreateDto} to a new {@link Author} entity.
     *
     * @param dto The source DTO containing the author's creation data.
     * @return A new, transient {@link Author} entity ready for persistence.
     */
    public Author toEntity(AuthorCreateDto dto) {
        Author author = new Author();
        author.setName(dto.name());
        author.setSummary(dto.summary());
        return author;
    }

    /**
     * Maps an {@link Author} entity and a page of their books to an {@link AuthorDetailsDto}.
     *
     * @param author The persistent {@link Author} entity.
     * @param books  A paginated list of the author's books.
     * @return A comprehensive {@link AuthorDetailsDto}.
     */
    public AuthorDetailsDto toDetailsDto(Author author, Page<BookSummaryDto> books) {
        return new AuthorDetailsDto(
                author.getId(),
                author.getName(),
                author.getSummary(),
                author.getImage() != null ? author.getImage().getUrl() : null,
                books
        );
    }

    /**
     * Maps an {@link Author} entity to an {@link AuthorSummaryDto} for list displays.
     *
     * @param author The persistent {@link Author} entity.
     * @return A lightweight {@link AuthorSummaryDto}.
     */
    public AuthorSummaryDto toSummaryDto(Author author) {
        return new AuthorSummaryDto(
                author.getId(),
                author.getName(),
                author.getImage() != null ? author.getImage().getUrl() : null
        );
    }
}
