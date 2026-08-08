package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.author.AuthorService;
import bg.softuni.bookshelf.service.author.dto.AuthorCreateDto;
import bg.softuni.bookshelf.service.author.dto.AuthorDetailsDto;
import bg.softuni.bookshelf.service.author.dto.AuthorSummaryDto;
import bg.softuni.bookshelf.service.author.dto.AuthorUpdateDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Administrative CRUD endpoints for authors. All operations require the ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/admin/authors", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Author API", description = "Administrative management of authors.")
public class AuthorController {

    private final AuthorService authorService;

    @Operation(
            operationId = "getAllAuthors",
            summary = "List authors",
            description = "Retrieves a paginated list of all authors in a summary format."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the paginated authors.")
    @GetMapping
    public ResponseEntity<PagedResponse<AuthorSummaryDto>> getAllAuthors(Pageable pageable) {
        log.info("API GET request to list authors.");
        Page<AuthorSummaryDto> page = authorService.getAll(pageable);
        return ResponseEntity.ok(PagedResponse.from(page));
    }

    @Operation(
            operationId = "getAuthorById",
            summary = "Get an author",
            description = "Retrieves a single author by their unique identifier, including a page of their books."
    )
    @ApiResponse(responseCode = "200", description = "Author found.")
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDetailsDto> getAuthorById(
            @Parameter(description = "The UUID of the author") @PathVariable UUID id,
            Pageable pageable) {
        log.info("API GET request for author {}.", id);
        return ResponseEntity.ok(authorService.getById(id, pageable));
    }

    @Operation(
            operationId = "createAuthor",
            summary = "Create an author",
            description = "Creates a new author and handles the optional profile image upload."
    )
    @ApiResponse(responseCode = "201", description = "Author created successfully.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthorDetailsDto> createAuthor(
            @Valid @RequestPart("author") AuthorCreateDto createDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        log.info("API POST request to create author [{}].", createDto.name());
        AuthorDetailsDto created = authorService.createAuthor(createDto, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            operationId = "updateAuthor",
            summary = "Update an author",
            description = "Partially updates an existing author's details and returns the updated resource."
    )
    @ApiResponse(responseCode = "200", description = "Author updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<AuthorDetailsDto> updateAuthor(
            @Parameter(description = "The UUID of the author") @PathVariable UUID id,
            @Valid @RequestBody AuthorUpdateDto updateDto) {
        log.info("API PUT request to update author {}.", id);
        return ResponseEntity.ok(authorService.updateAuthor(id, updateDto));
    }

    @Operation(
            operationId = "deleteAuthor",
            summary = "Delete an author",
            description = "Deletes an author. Fails if the author is still referenced by existing books."
    )
    @ApiResponse(responseCode = "204", description = "Author deleted successfully.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "The UUID of the author") @PathVariable UUID id) {
        log.info("API DELETE request to delete author {}.", id);
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}