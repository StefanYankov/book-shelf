package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import bg.softuni.bookshelf.service.bookshelf.BookshelfService;
import bg.softuni.bookshelf.service.bookshelf.dto.*;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/api/users/me/shelves", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@Tag(name = "User Shelf API", description = "Endpoints for managing the authenticated user's personal bookshelves.")
public class UserShelfController {

    private final BookshelfService bookshelfService;

    @Operation(
            operationId = "getUserShelves",
            summary = "Get all shelves for the current user",
            description = "Retrieves a paginated list of all bookshelves created by the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user's shelves")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookshelfSummaryDto>> getUserShelves(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        Page<BookshelfSummaryDto> shelves = bookshelfService.getShelvesForUser(principal.getId(), pageable);
        return ResponseEntity.ok(shelves);
    }

    @Operation(
            operationId = "createShelf",
            summary = "Create a new bookshelf",
            description = "Creates a new, empty bookshelf for the authenticated user."
    )
    @ApiResponse(responseCode = "201", description = "Bookshelf created successfully")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookshelfDetailsDto> createShelf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BookshelfCreateDto createDto) {
        BookshelfDetailsDto newShelf = bookshelfService.createShelf(createDto, principal.getId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newShelf.id()).toUri();
        return ResponseEntity.created(location).body(newShelf);
    }

    @Operation(
            operationId = "getShelfById",
            summary = "Get a specific bookshelf",
            description = "Retrieves the details of a single bookshelf, including the books it contains."
    )
    @ApiResponse(responseCode = "200", description = "Bookshelf found")
    @GetMapping("/{shelfId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookshelfDetailsDto> getShelfById(@Parameter(description = "The UUID of the shelf") @PathVariable UUID shelfId) {
        BookshelfDetailsDto shelf = bookshelfService.getShelfById(shelfId);
        return ResponseEntity.ok(shelf);
    }

    @Operation(
            operationId = "updateShelf",
            summary = "Update a bookshelf",
            description = "Updates the details (e.g., name) of an existing bookshelf."
    )
    @ApiResponse(responseCode = "200", description = "Bookshelf updated successfully")
    @PutMapping("/{shelfId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookshelfDetailsDto> updateShelf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "The UUID of the shelf") @PathVariable UUID shelfId,
            @Valid @RequestBody BookshelfUpdateDto updateDto) {
        BookshelfDetailsDto updatedShelf = bookshelfService.updateShelf(shelfId, updateDto);
        return ResponseEntity.ok(updatedShelf);
    }

    @Operation(
            operationId = "deleteShelf",
            summary = "Delete a bookshelf",
            description = "Deletes an existing bookshelf. This action cannot be undone."
    )
    @ApiResponse(responseCode = "204", description = "Bookshelf deleted successfully")
    @DeleteMapping("/{shelfId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteShelf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "The UUID of the shelf") @PathVariable UUID shelfId) {
        bookshelfService.deleteShelf(shelfId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            operationId = "addBookToShelf",
            summary = "Add a book to a shelf",
            description = "Adds a book to a specific bookshelf."
    )
    @ApiResponse(responseCode = "204", description = "Book added successfully")
    @PostMapping("/{shelfId}/books")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addBookToShelf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "The UUID of the shelf") @PathVariable UUID shelfId,
            @Valid @RequestBody AddBookToBookshelfDto addBookDto) {
        bookshelfService.addBookToShelf(shelfId, addBookDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            operationId = "removeBookFromShelf",
            summary = "Remove a book from a shelf",
            description = "Removes a book from a specific bookshelf."
    )
    @ApiResponse(responseCode = "204", description = "Book removed successfully")
    @DeleteMapping("/{shelfId}/books/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBookFromShelf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "The UUID of the shelf") @PathVariable UUID shelfId,
            @Parameter(description = "The UUID of the book to remove") @PathVariable UUID bookId) {
        bookshelfService.removeBookFromShelf(shelfId, bookId);
        return ResponseEntity.noContent().build();
    }
}
