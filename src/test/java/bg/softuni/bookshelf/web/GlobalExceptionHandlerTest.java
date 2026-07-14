package bg.softuni.bookshelf.web;

import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {
    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ------------------------------------------------------------------
    // Stub controller: one endpoint per exception branch under test.
    // Each method exists ONLY to trigger the matching @ExceptionHandler.
    // ------------------------------------------------------------------
    @RestController
    static class TestController {

        @GetMapping("/test/business-exception")
        public void throwBusinessException() {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }

        @GetMapping("/test/bad-credentials")
        public void throwBadCredentials() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/test/disabled")
        public void throwDisabled() {
            throw new DisabledException("Account disabled");
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Access is denied");
        }

        @GetMapping("/test/expired-jwt")
        public void throwExpiredJwt() {
            // Header + Claims can be null for a pure handler test; only the message is asserted-agnostic.
            throw new ExpiredJwtException(null, null, "JWT expired");
        }

        @GetMapping("/test/generic")
        public void throwGeneric() {
            throw new IllegalStateException("Something unexpected blew up");
        }

        // Used for BOTH the @Valid failure (400 + field map) AND the malformed-JSON case.
        @PostMapping("/test/validate")
        public void validateBody(@Valid @RequestBody SampleRequest request) {
            // no-op: reaching here means validation passed
        }

        @GetMapping("/test/business/{errorCode}")
        public void throwBusinessByCode(@PathVariable String errorCode) {
            throw new BusinessException(ErrorCode.valueOf(errorCode));
        }

        // Minimal DTO with a single constraint to force a predictable field error.
        record SampleRequest(@NotBlank(message = "name must not be blank") String name) {
        }
    }

    // ------------------------------------------------------------------
    // BusinessException: verify the DYNAMIC status/code wiring holds
    // across ErrorCodes that map to DIFFERENT HTTP statuses.
    // One parameterized test replaces N near-identical copies.
    // ------------------------------------------------------------------
    @ParameterizedTest(name = "{0} -> HTTP {1}")
    @MethodSource("businessErrorCases")
    @DisplayName("Should map each ErrorCode to its declared status and code")
    void shouldMapBusinessExceptionStatuses(ErrorCode errorCode) throws Exception {
        mockMvc.perform(get("/test/business/" + errorCode.name()))
                .andExpect(status().is(errorCode.getHttpStatus().value()))
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                .andExpect(jsonPath("$.status").value(errorCode.getHttpStatus().value()))
                .andExpect(jsonPath("$.detail").value(errorCode.getDefaultMessage()))
                .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()));
    }

    // Deliberately spans 404, 409, 401, 403 to prove it's not hard-coded to one status.
    static Stream<ErrorCode> businessErrorCases() {
        return Stream.of(
                ErrorCode.BOOK_NOT_FOUND,          // 404
                ErrorCode.DUPLICATE_REVIEW,        // 409
                ErrorCode.INVALID_CREDENTIALS,     // 401
                ErrorCode.SELF_LOCK_PREVENTION     // 403
        );
    }

    // ------------------------------------------------------------------
    // 1. Business rule violations (dynamic status from ErrorCode)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Should handle BusinessException and return correct ProblemDetail")
    void shouldHandleBusinessException() throws Exception {
        mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Book not found."))
                .andExpect(jsonPath("$.errorCode").value("E1100"));
    }

    // ------------------------------------------------------------------
    // 2. Authentication: bad credentials -> 401
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Should handle BadCredentialsException as 401 with generic message")
    void shouldHandleBadCredentials() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                // Assert we DON'T leak whether it was the username or password that failed.
                .andExpect(jsonPath("$.detail").value("Invalid username or password."));
    }

    // ------------------------------------------------------------------
    // 3. Authentication: disabled account -> 403
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Should handle DisabledException as 403 Account Disabled")
    void shouldHandleDisabledUser() throws Exception {
        mockMvc.perform(get("/test/disabled"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Account Disabled"))
                .andExpect(jsonPath("$.status").value(403));
    }

    // ------------------------------------------------------------------
    // 4. Authorization: @PreAuthorize denial -> 403
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Should handle AccessDeniedException as 403 Forbidden")
    void shouldHandleAccessDenied() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403));
    }

    // ------------------------------------------------------------------
    // 5. Expired token -> 401 (triggers frontend logout flow)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Should handle ExpiredJwtException as 401 Token Expired")
    void shouldHandleExpiredJwt() throws Exception {
        mockMvc.perform(get("/test/expired-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Token Expired"))
                .andExpect(jsonPath("$.status").value(401));
    }

    // ------------------------------------------------------------------
    // 6. Catch-all: unexpected bug -> 500 (details must NOT leak)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Should handle unexpected Exception as 500 without leaking internals")
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                // Assert the raw exception message is NOT reflected back to the client.
                .andExpect(jsonPath("$.detail").value("An unexpected internal error occurred."));
    }

    // ------------------------------------------------------------------
    // 7a. Validation failure -> 400 with your custom field-error map
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Should handle MethodArgumentNotValid with field-level error map")
    void shouldHandleValidationErrors() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}")) // blank -> violates @NotBlank
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Input validation failed"))
                // The custom Angular-friendly map: field name -> message.
                .andExpect(jsonPath("$.errors.name").value("name must not be blank"));
    }

    // ------------------------------------------------------------------
    // 7b. Malformed JSON -> 400 (proves the Exception.class catch-all
    //     does NOT swallow Spring's built-in handler and turn it into 500)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Should return 400 (not 500) for malformed JSON body")
    void shouldHandleMalformedJson() throws Exception {
        // Regression guard: proves catch(Exception.class) does NOT swallow
        // HttpMessageNotReadableException and downgrade it to a 500.
        String malformedJson = "{".concat(" this is not valid json ");

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}