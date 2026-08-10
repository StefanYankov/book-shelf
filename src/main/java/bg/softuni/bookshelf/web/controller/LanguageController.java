package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.language.LanguageService;
import bg.softuni.bookshelf.service.language.dto.LanguageCreateDto;
import bg.softuni.bookshelf.service.language.dto.LanguageDto;
import bg.softuni.bookshelf.service.language.dto.LanguageUpdateDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Administrative CRUD endpoints for languages. All operations require the ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/admin/languages", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Language API", description = "Administrative management of languages.")
public class LanguageController {

    private final LanguageService languageService;

    @Operation(
            operationId = "getAllLanguages",
            summary = "List languages",
            description = "Retrieves a paginated list of all languages."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the paginated languages.")
    @GetMapping
    public ResponseEntity<PagedResponse<LanguageDto>> getAllLanguages(@ParameterObject Pageable pageable) {
        log.info("API GET request to list languages.");
        Page<LanguageDto> page = languageService.getAll(pageable);
        return ResponseEntity.ok(PagedResponse.from(page));
    }

    @Operation(
            operationId = "getLanguageById",
            summary = "Get a language",
            description = "Retrieves a single language by its unique identifier."
    )
    @ApiResponse(responseCode = "200", description = "Language found.")
    @GetMapping("/{id}")
    public ResponseEntity<LanguageDto> getLanguageById(
            @Parameter(description = "The UUID of the language") @PathVariable UUID id) {
        log.info("API GET request for language {}.", id);
        return ResponseEntity.ok(languageService.getById(id));
    }

    @Operation(
            operationId = "createLanguage",
            summary = "Create a language",
            description = "Creates a new language. The name must be unique (case-insensitive)."
    )
    @ApiResponse(responseCode = "201", description = "Language created successfully.")
    @PostMapping
    public ResponseEntity<LanguageDto> createLanguage(@Valid @RequestBody LanguageCreateDto createDto) {
        log.info("API POST request to create language [{}].", createDto.name());
        LanguageDto created = languageService.createLanguage(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            operationId = "updateLanguage",
            summary = "Update a language",
            description = "Partially updates an existing language and returns the updated resource."
    )
    @ApiResponse(responseCode = "200", description = "Language updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<LanguageDto> updateLanguage(
            @Parameter(description = "The UUID of the language") @PathVariable UUID id,
            @Valid @RequestBody LanguageUpdateDto updateDto) {
        log.info("API PUT request to update language {}.", id);
        return ResponseEntity.ok(languageService.updateLanguage(id, updateDto));
    }

    @Operation(
            operationId = "deleteLanguage",
            summary = "Delete a language",
            description = "Deletes a language. Fails if the language is still referenced by existing books."
    )
    @ApiResponse(responseCode = "204", description = "Language deleted successfully.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLanguage(
            @Parameter(description = "The UUID of the language") @PathVariable UUID id) {
        log.info("API DELETE request to delete language {}.", id);
        languageService.deleteLanguage(id);
        return ResponseEntity.noContent().build();
    }
}
