package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.genre.GenreService;
import bg.softuni.bookshelf.service.genre.dto.GenreCreateDto;
import bg.softuni.bookshelf.service.genre.dto.GenreDto;
import bg.softuni.bookshelf.service.genre.dto.GenreUpdateDto;
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

import java.util.UUID;

/**
 * Administrative CRUD endpoints for genres. All operations require the ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/admin/genres", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Genre API", description = "Administrative management of genres.")
public class GenreController {

    private final GenreService genreService;

    @Operation(
            operationId = "getAllGenres",
            summary = "List genres",
            description = "Retrieves a paginated list of all genres."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the paginated genres.")
    @GetMapping
    public ResponseEntity<PagedResponse<GenreDto>> getAllGenres(Pageable pageable) {
        log.info("API GET request to list genres.");
        Page<GenreDto> page = genreService.getAll(pageable);
        return ResponseEntity.ok(PagedResponse.from(page));
    }

    @Operation(
            operationId = "getGenreById",
            summary = "Get a genre",
            description = "Retrieves a single genre by its unique identifier."
    )
    @ApiResponse(responseCode = "200", description = "Genre found.")
    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> getGenreById(
            @Parameter(description = "The UUID of the genre") @PathVariable UUID id) {
        log.info("API GET request for genre {}.", id);
        return ResponseEntity.ok(genreService.getById(id));
    }

    @Operation(
            operationId = "createGenre",
            summary = "Create a genre",
            description = "Creates a new genre. The name must be unique (case-insensitive)."
    )
    @ApiResponse(responseCode = "201", description = "Genre created successfully.")
    @PostMapping
    public ResponseEntity<GenreDto> createGenre(@Valid @RequestBody GenreCreateDto createDto) {
        log.info("API POST request to create genre [{}].", createDto.name());
        GenreDto created = genreService.createGenre(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            operationId = "updateGenre",
            summary = "Update a genre",
            description = "Partially updates an existing genre and returns the updated resource."
    )
    @ApiResponse(responseCode = "200", description = "Genre updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<GenreDto> updateGenre(
            @Parameter(description = "The UUID of the genre") @PathVariable UUID id,
            @Valid @RequestBody GenreUpdateDto updateDto) {
        log.info("API PUT request to update genre {}.", id);
        return ResponseEntity.ok(genreService.updateGenre(id, updateDto));
    }

    @Operation(
            operationId = "deleteGenre",
            summary = "Delete a genre",
            description = "Deletes a genre. Fails if the genre is still referenced by existing books."
    )
    @ApiResponse(responseCode = "204", description = "Genre deleted successfully.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(
            @Parameter(description = "The UUID of the genre") @PathVariable UUID id) {
        log.info("API DELETE request to delete genre {}.", id);
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}