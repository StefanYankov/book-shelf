package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import bg.softuni.bookshelf.service.review.ReviewService;
import bg.softuni.bookshelf.service.review.dto.ReviewCreateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewUpdateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewViewDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@Tag(name = "Review API", description = "Endpoints for managing reviews.")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            operationId = "getReviewsForTarget",
            summary = "Get reviews for a specific target",
            description = "Retrieves a paginated list of reviews for a given target entity (e.g., a book)."
    )
    @GetMapping
    public ResponseEntity<PagedResponse<ReviewViewDto>> getReviewsForTarget(
            @RequestParam UUID targetId,
            @RequestParam String targetType,
            Pageable pageable) {
        Page<ReviewViewDto> reviews = reviewService.getReviewsForTarget(targetId, targetType, pageable);
        return ResponseEntity.ok(PagedResponse.from(reviews));
    }

    @Operation(
            operationId = "addReview",
            summary = "Add a review for a target",
            description = "Creates a new review for a specified target entity. A user can only review a target once."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewViewDto> addReview(
            @RequestParam UUID targetId,
            @RequestParam String targetType,
            @Valid @RequestBody ReviewCreateDto createDto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        ReviewViewDto review = reviewService.addReview(createDto, targetId, targetType, principal.getId());
        return ResponseEntity.ok(review);
    }

    @Operation(
            operationId = "updateReview",
            summary = "Update an existing review",
            description = "Updates the content of a review. Only the author of the review can perform this action."
    )
    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewViewDto> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateDto updateDto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        ReviewViewDto review = reviewService.updateReview(reviewId, updateDto, principal.getId());
        return ResponseEntity.ok(review);
    }

    @Operation(
            operationId = "deleteReview",
            summary = "Delete a review",
            description = "Deletes a review. This can be done by the author of the review or by an administrator."
    )
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        // The controller is the correct boundary to translate a security principal
        // into the plain values the service understands.
        reviewService.deleteReview(reviewId, principal.getId(), principal.isAdmin());
        return ResponseEntity.noContent().build();
    }
}