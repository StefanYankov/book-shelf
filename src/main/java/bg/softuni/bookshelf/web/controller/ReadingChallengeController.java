package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import bg.softuni.bookshelf.service.challenge.ReadingChallengeProxyService;
import bg.softuni.bookshelf.service.challenge.dto.LogProgressDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeCreateDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeViewDto;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller exposing reading challenge operations to the frontend. Requests are
 * proxied to the reading challenge microservice via Feign, with the caller's JWT forwarded.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/challenges", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@Tag(name = "Reading Challenge API", description = "Endpoints for creating and progressing reading challenges via the reading challenge microservice.")
public class ReadingChallengeController {

    private final ReadingChallengeProxyService readingChallengeProxyService;

    @Operation(
            operationId = "createChallenge",
            summary = "Create a reading challenge",
            description = "Creates a yearly reading challenge for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Challenge created"),
            @ApiResponse(responseCode = "409", description = "A challenge already exists for this user and year")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReadingChallengeViewDto> createChallenge(
            @Valid @RequestBody ReadingChallengeCreateDto createDto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API POST request to create a challenge for user {}", principal.getId());
        ReadingChallengeViewDto created = readingChallengeProxyService.createChallenge(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            operationId = "logProgress",
            summary = "Log reading progress",
            description = "Adds progress to a challenge, completing it when the goal is reached."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress logged"),
            @ApiResponse(responseCode = "404", description = "Challenge not found")
    })
    @PutMapping("/{challengeId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReadingChallengeViewDto> logProgress(
            @Parameter(description = "The UUID of the challenge") @PathVariable UUID challengeId,
            @Valid @RequestBody LogProgressDto progressDto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API PUT request to log progress for challenge {} by user {}", challengeId, principal.getId());
        ReadingChallengeViewDto updated = readingChallengeProxyService.logProgress(challengeId, progressDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            operationId = "getChallenge",
            summary = "Get a challenge by year",
            description = "Retrieves the authenticated user's challenge for a given year."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Challenge found"),
            @ApiResponse(responseCode = "404", description = "Challenge not found")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReadingChallengeViewDto> getChallenge(
            @Parameter(description = "The challenge year") @RequestParam int year,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API GET request for challenge (year {}) by user {}", year, principal.getId());
        return ResponseEntity.ok(readingChallengeProxyService.getChallenge(year));
    }
}