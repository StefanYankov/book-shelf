package bg.softuni.bookshelf.service.bookshelf.dto;

import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.UUID;

@Builder
public record BookshelfDetailsDto(
    UUID id,
    String name,
    String description,
    Page<BookSummaryDto> books

) {

}
