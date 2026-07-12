package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookUpdateDto;
import bg.softuni.bookshelf.service.bookshelf.BookshelfService;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfUpdateDto;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/api/admin/moderation", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Moderation API", description = "Endpoints for system-wide administrative content curation and moderation.")
public class AdminModerationController {

    private final BookshelfService bookshelfService;
    private final BookService bookService;

    @Operation(
            operationId = "moderateBook",
            summary = "Forcibly moderate book details",
            description = "Rewrites a book's metadata (e.g. title, author, genres, format) to sanitize profanity or fix editorial issues."
    )
    @ApiResponse(responseCode = "200", description = "Book moderated successfully.")
    @PutMapping("/books/{bookId}")
    public ResponseEntity<BookDetailsDto> moderateBook(
            @Parameter(description = "The UUID of the book to moderate") @PathVariable UUID bookId,
            @Valid @RequestBody BookUpdateDto updateDto) {
        log.warn("ADMIN ACTION: Moderating book {} details.", bookId);
        BookDetailsDto moderatedBook = bookService.moderateBook(bookId, updateDto);
        return ResponseEntity.ok(moderatedBook);
    }

    @Operation(
            operationId = "moderateShelf",
            summary = "Forcibly moderate bookshelf details",
            description = "Rewrites a user's bookshelf details (e.g. name, description) to remove offensive content."
    )
    @ApiResponse(responseCode = "200", description = "Bookshelf moderated successfully.")
    @PutMapping("/shelves/{shelfId}")
    public ResponseEntity<BookshelfDetailsDto> moderateShelf(
            @Parameter(description = "The UUID of the shelf to moderate") @PathVariable UUID shelfId,
            @Valid @RequestBody BookshelfUpdateDto updateDto) {
        log.warn("ADMIN ACTION: Moderating bookshelf {} details.", shelfId);
        BookshelfDetailsDto moderatedShelf = bookshelfService.updateShelf(shelfId, updateDto);
        return ResponseEntity.ok(moderatedShelf);
    }

    @Operation(
            operationId = "forceDeleteShelf",
            summary = "Forcibly delete bookshelf",
            description = "Permanently deletes offensive user bookshelves. Associations with books are cleaned automatically."
    )
    @ApiResponse(responseCode = "204", description = "Bookshelf deleted successfully.")
    @DeleteMapping("/shelves/{shelfId}")
    public ResponseEntity<Void> forceDeleteShelf(
            @Parameter(description = "The UUID of the shelf to delete") @PathVariable UUID shelfId) {
        log.warn("ADMIN ACTION: Forcibly deleting bookshelf {}.", shelfId);
        bookshelfService.deleteShelf(shelfId);
        return ResponseEntity.noContent().build();
    }
}
