package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import bg.softuni.bookshelf.service.user.UserService;
import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.LockUserRequestDto;
import bg.softuni.bookshelf.service.user.dto.PermissionRequestDto;
import bg.softuni.bookshelf.service.user.dto.UserPermissionsDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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
    public ResponseEntity<PagedResponse<AdminUserViewDto>> getAllUsers(@ParameterObject Pageable pageable) {
        log.info("API GET request to retrieve all users for admin.");
        Page<AdminUserViewDto> userPage = userService.getAllUsers(pageable);
        return ResponseEntity.ok(PagedResponse.from(userPage));
    }

    @Operation(
            operationId = "lockUser",
            summary = "Lock a user account",
            description = "Locks a user's account, preventing them from logging in. Provide a positive "
                    + "lockDurationHours for a temporary lock; omit it for a permanent lock."
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
        Duration duration = dto.lockDurationHours() == null
                ? null
                : Duration.ofHours(dto.lockDurationHours());
        userService.lockUser(userId, dto.reason(), principal.getId(), duration);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            operationId = "unlockUser",
            summary = "Unlock a user account",
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

    @Operation(
            operationId = "grantPermission",
            summary = "Grant a permission to a user",
            description = "Grants a fine-grained permission (e.g. review moderation) to a standard user account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Permission successfully granted."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The target is not a standard user account.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Target user or executing administrator record not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/users/{userId}/permissions")
    public ResponseEntity<Void> grantPermission(
            @Parameter(description = "The UUID of the user to grant the permission to.") @PathVariable UUID userId,
            @Valid @RequestBody PermissionRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API POST request to grant permission {} to user {} by admin {}.", dto.permission(), userId, principal.getUsername());
        userService.grantPermission(userId, dto.permission(), dto.reason(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            operationId = "revokePermission",
            summary = "Revoke a permission from a user",
            description = "Revokes a previously granted fine-grained permission from a standard user account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Permission successfully revoked."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The target is not a standard user account.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Target user or executing administrator record not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/users/{userId}/permissions")
    public ResponseEntity<Void> revokePermission(
            @Parameter(description = "The UUID of the user to revoke the permission from.") @PathVariable UUID userId,
            @Valid @RequestBody PermissionRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API DELETE request to revoke permission {} from user {} by admin {}.", dto.permission(), userId, principal.getUsername());
        userService.revokePermission(userId, dto.permission(), dto.reason(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            operationId = "getUserPermissions",
            summary = "Get a user's permissions",
            description = "Retrieves the fine-grained permissions currently granted to a standard user account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the user's permissions."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The target is not a standard user account.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Target user not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<UserPermissionsDto> getUserPermissions(
            @Parameter(description = "The UUID of the user whose permissions are requested.") @PathVariable UUID userId) {
        log.info("API GET request to retrieve permissions for user {}.", userId);
        return ResponseEntity.ok(userService.getUserPermissions(userId));
    }
}