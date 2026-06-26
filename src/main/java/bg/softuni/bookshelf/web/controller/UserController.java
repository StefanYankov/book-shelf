package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import bg.softuni.bookshelf.service.auth.JwtService;
import bg.softuni.bookshelf.service.auth.dto.AuthenticationResponse;
import bg.softuni.bookshelf.service.user.UserService;
import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "User API", description = "Endpoints for managing the authenticated user's profile.")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Operation(
            operationId = "getMyProfile",
            summary = "Get current user profile",
            description = "Retrieves the profile information for the currently authenticated user."
    )
    @ApiStandardResponses
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfileDto> getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        log.info("API GET request to retrieve profile for user {}", principal.getUsername());
        return ResponseEntity.ok(userService.getProfile(principal.getId()));
    }

    @Operation(
            operationId = "updateMyProfile",
            summary = "Update current user profile",
            description = "Updates the first and last name for the currently authenticated user."
    )
    @ApiResponse(responseCode = "204", description = "Profile updated successfully.")
    @ApiStandardResponses
    @PutMapping("/me")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateProfileDto dto) {
        log.info("API PUT request to update profile for user {}", principal.getUsername());
        userService.updateProfile(principal.getId(), dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            operationId = "changeMyPassword",
            summary = "Change current user password",
            description = "Changes the password for the currently authenticated user and returns a fresh JWT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully. Returns updated stateless token context."
            )
    })
    @ApiStandardResponses
    @PutMapping("/me/password")
    public ResponseEntity<AuthenticationResponse> changeMyPassword(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChangePasswordDto dto) {
        log.info("API PUT request to change password for user {}", principal.getUsername());
        
        userService.changePassword(principal.getId(), dto);
        
        String freshToken = jwtService.generateToken(principal);
        
        return ResponseEntity.ok(new AuthenticationResponse(freshToken));
    }
}
