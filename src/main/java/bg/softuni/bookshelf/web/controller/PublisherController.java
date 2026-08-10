package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.publisher.PublisherService;
import bg.softuni.bookshelf.service.publisher.dto.PublisherCreateDto;
import bg.softuni.bookshelf.service.publisher.dto.PublisherDto;
import bg.softuni.bookshelf.service.publisher.dto.PublisherUpdateDto;
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
 * Administrative CRUD endpoints for publishers. All operations require the ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/admin/publishers", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Publisher API", description = "Administrative management of publishers.")
public class PublisherController {

    private final PublisherService publisherService;

    @Operation(
            operationId = "getAllPublishers",
            summary = "List publishers",
            description = "Retrieves a paginated list of all publishers."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the paginated publishers.")
    @GetMapping
    public ResponseEntity<PagedResponse<PublisherDto>> getAllPublishers(@ParameterObject Pageable pageable) {
        log.info("API GET request to list publishers.");
        Page<PublisherDto> page = publisherService.getAll(pageable);
        return ResponseEntity.ok(PagedResponse.from(page));
    }

    @Operation(
            operationId = "getPublisherById",
            summary = "Get a publisher",
            description = "Retrieves a single publisher by its unique identifier."
    )
    @ApiResponse(responseCode = "200", description = "Publisher found.")
    @GetMapping("/{id}")
    public ResponseEntity<PublisherDto> getPublisherById(
            @Parameter(description = "The UUID of the publisher") @PathVariable UUID id) {
        log.info("API GET request for publisher {}.", id);
        return ResponseEntity.ok(publisherService.getById(id));
    }

    @Operation(
            operationId = "createPublisher",
            summary = "Create a publisher",
            description = "Creates a new publisher. The name must be unique (case-insensitive)."
    )
    @ApiResponse(responseCode = "201", description = "Publisher created successfully.")
    @PostMapping
    public ResponseEntity<PublisherDto> createPublisher(@Valid @RequestBody PublisherCreateDto createDto) {
        log.info("API POST request to create publisher [{}].", createDto.name());
        PublisherDto created = publisherService.createPublisher(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            operationId = "updatePublisher",
            summary = "Update a publisher",
            description = "Partially updates an existing publisher and returns the updated resource."
    )
    @ApiResponse(responseCode = "200", description = "Publisher updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<PublisherDto> updatePublisher(
            @Parameter(description = "The UUID of the publisher") @PathVariable UUID id,
            @Valid @RequestBody PublisherUpdateDto updateDto) {
        log.info("API PUT request to update publisher {}.", id);
        return ResponseEntity.ok(publisherService.updatePublisher(id, updateDto));
    }

    @Operation(
            operationId = "deletePublisher",
            summary = "Delete a publisher",
            description = "Deletes a publisher. Fails if the publisher is still referenced by existing books."
    )
    @ApiResponse(responseCode = "204", description = "Publisher deleted successfully.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublisher(
            @Parameter(description = "The UUID of the publisher") @PathVariable UUID id) {
        log.info("API DELETE request to delete publisher {}.", id);
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}
