package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import bg.softuni.bookshelf.service.user.UserService;
import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.LockUserRequestDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin API", description = "Endpoints for administrative operations.")
public class AdminController {

    private final UserService userService;

    @Operation(
            operationId = "getAllUsers",
            summary = "Get all users",
            description = "Retrieves a paginated list of all users in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved paginated users array."
            )
    })
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResponse<AdminUserViewDto>> getAllUsers(Pageable pageable) {
        log.info("API GET request to retrieve all users for admin.");
        Page<AdminUserViewDto> userPage = userService.getAllUsers(pageable);
        return ResponseEntity.ok(PagedResponse.from(userPage));
    }

    @Operation(
            operationId = "lockUser",
            summary = "Lock a user account",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "Locks a user's account, preventing them from logging in."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User account successfully locked."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Target user or executing administrator record not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/users/{userId}/lock")
    public ResponseEntity<Void> lockUser(
            @Parameter(description = "The UUID of the user to lock.") @PathVariable UUID userId,
            @Valid @RequestBody LockUserRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API POST request to lock user {} by admin {}.", userId, principal.getUsername());
        userService.lockUser(userId, dto.reason(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            operationId = "unlockUser",
            summary = "Unlock a user account",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "Unlocks a previously locked user account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User account successfully unlocked."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Target user or executing administrator record not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<Void> unlockUser(
            @Parameter(description = "The UUID of the user to unlock.") @PathVariable UUID userId,
            @Valid @RequestBody LockUserRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API POST request to unlock user {} by admin {}.", userId, principal.getUsername());
        userService.unlockUser(userId, dto.reason(), principal.getId());
        return ResponseEntity.noContent().build();
    }
}