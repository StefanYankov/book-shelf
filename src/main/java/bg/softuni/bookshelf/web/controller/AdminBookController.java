package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.book.dto.BookCreateDto;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookUpdateDto;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Administrative CRUD endpoints for books. All operations require the ADMIN role.
 * Creation accepts an optional cover image as a multipart request, mirroring author creation.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/admin/books", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Book API", description = "Administrative management of books.")
public class AdminBookController {

    private final BookService bookService;

    @Operation(
            operationId = "createBook",
            summary = "Create a book",
            description = "Creates a new book with an optional cover image. Author, language, publisher, and genres must reference existing records."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully."),
            @ApiResponse(
                    responseCode = "404",
                    description = "A referenced author, language, publisher, or genre was not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDetailsDto> createBook(
            @Valid @RequestPart("book") BookCreateDto createDto,
            @RequestPart(value = "image", required = false) MultipartFile coverImageFile) {
        log.info("API POST request to create book [{}].", createDto.title());
        BookDetailsDto created = bookService.createBook(createDto, coverImageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            operationId = "updateBook",
            summary = "Update a book",
            description = "Partially updates an existing book and returns the updated resource."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully."),
            @ApiResponse(
                    responseCode = "404",
                    description = "The book, or a referenced author, language, publisher, or genre was not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookDetailsDto> updateBook(
            @Parameter(description = "The UUID of the book") @PathVariable UUID id,
            @Valid @RequestBody BookUpdateDto updateDto) {
        log.info("API PUT request to update book {}.", id);
        return ResponseEntity.ok(bookService.updateBook(id, updateDto));
    }

    @Operation(
            operationId = "deleteBook",
            summary = "Delete a book",
            description = "Deletes a book and schedules removal of its cover image after the deletion commits."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully."),
            @ApiResponse(
                    responseCode = "404",
                    description = "The book was not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "The UUID of the book") @PathVariable UUID id) {
        log.info("API DELETE request to delete book {}.", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
