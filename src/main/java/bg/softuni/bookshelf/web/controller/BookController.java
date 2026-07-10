package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookSearchFilters;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing the creation and retrieval of logistics books.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/books", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@Tag(name = "Book API", description = "Endpoints for creating and managing books.")
public class BookController {

    private final BookService bookService;

    @Operation(
            operationId = "getAllBooks",
            summary = "Get all books",
            description = "Retrieves a paginated list of all books."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of books")
    @GetMapping
    public ResponseEntity<PagedResponse<BookSummaryDto>> getAllBooks(Pageable pageable) {
        log.info("API GET request for all books, pageable: {}", pageable);
        Page<BookSummaryDto> bookPage = bookService.getAll(pageable);
        return ResponseEntity.ok(PagedResponse.from(bookPage));
    }

    @Operation(
            operationId = "getBookById",
            summary = "Get book by ID",
            description = "Retrieves a specific book."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDetailsDto> getBookById(
            @Parameter(description = "The UUID of the book") @PathVariable UUID id
    ){
        log.info("API GET request for book ID: {}", id);
        BookDetailsDto book = bookService.getById(id);
        return ResponseEntity.ok(book);
    }

    @Operation(
            operationId = "searchBooks",
            summary = "Advanced book search",
            description = "Performs a faceted search across the book catalog."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results retrieved")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<BookSummaryDto>> searchBooks(
            @ParameterObject @Valid BookSearchFilters filters,
            Pageable pageable
    ) {
        log.info("Faceted catalog search request. Filters: {}", filters);
        return ResponseEntity.ok(bookService.searchBooks(filters, pageable));
    }
}
