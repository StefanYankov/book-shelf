package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.AuthenticationService;
import bg.softuni.bookshelf.service.auth.dto.*;
import bg.softuni.bookshelf.web.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling public authentication and user lifecycle endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Endpoints for user registration, login, and password management.")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account in a pending (unverified) state and dispatches a verification email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully registered. A JWT is returned for immediate (but restricted) login."),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g., missing or invalid fields)."),
            @ApiResponse(responseCode = "409", description = "Conflict - Username or email already exists.")
    })
    @ApiStandardResponses
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("API POST request to register new user: {}", request.username());
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Authenticate user",
            description = "Validates username and password. Returns a JWT if successful."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated."),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g., missing fields)."),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive/unverified account.")
    })
    @ApiStandardResponses
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        log.info("API POST request to authenticate user: {}", request.username());
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Verify user email",
            description = "Activates a user account by validating a secure email verification token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email successfully verified."),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token.")
    })
    @ApiStandardResponses
    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        log.info("API GET request to verify email with token");
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Request password reset",
            description = "Initiates a password reset flow. A link will be sent to the user's email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the email exists, a reset link was dispatched. This endpoint always returns 200 OK to prevent email enumeration attacks."),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g., invalid email format).")
    })
    @ApiStandardResponses
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        log.info("API POST request to initiate password reset for email: {}", request.email());
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Execute password reset",
            description = "Sets a new password using a secure, time-bound reset token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password successfully reset."),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token, or validation failed.")
    })
    @ApiStandardResponses
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        log.info("API POST request to execute password reset");
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
