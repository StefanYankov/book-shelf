package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import bg.softuni.bookshelf.service.auth.JwtService;
import bg.softuni.bookshelf.service.auth.dto.AuthenticationResponse;
import bg.softuni.bookshelf.service.user.UserService;
import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserSecurityDto;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiStandardResponses
@RequiredArgsConstructor
@Tag(name = "User Profile API", description = "Endpoints for user profile and credentials management.")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Operation(
            operationId = "getMyProfile",
            summary = "Get current user profile",
            description = "Retrieves the profile information for the currently authenticated user."
    )
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API GET request for user profile details: {}", principal.getUsername());
        return ResponseEntity.ok(userService.getProfile(principal.getId()));
    }

    @Operation(
            operationId = "updateMyProfile",
            summary = "Update current user profile",
            description = "Updates the first and last name for the currently authenticated user."
    )
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateProfileDto dto) {
        log.info("API PUT request to update profile details for: {}", principal.getUsername());
        userService.updateProfile(principal.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            operationId = "changeMyPassword",
            summary = "Change account password",
            description = "Changes the password for the currently authenticated user and returns a fresh JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated and fresh token returned")
    })
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthenticationResponse> changeMyPassword(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChangePasswordDto dto) {
        log.info("API PUT request to change password for user: {}", principal.getUsername());

        UserSecurityDto updatedUser = userService.changePassword(principal.getId(), dto);
        String freshToken = jwtService.generateTokenForUser(updatedUser, principal.getAuthorities());
        return ResponseEntity.ok(new AuthenticationResponse(freshToken));
    }
}
