package bg.softuni.bookshelf.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Centralized registry of all business error codes across the application.
 * Ensures API responses are strictly typed, predictable, and fully translatable by the frontend.
 */
@Getter
public enum ErrorCode {

    // --- System / General (E0000 - E0999) ---
    INTERNAL_SERVER_ERROR("E0000", "An unexpected internal error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND("E0001", "The requested resource could not be found.", HttpStatus.NOT_FOUND),
    VALIDATION_FAILED("E0002", "Input validation failed.", HttpStatus.BAD_REQUEST),

    // --- Book domain (E1000 - E1999) ----
    BOOK_NOT_FOUND("E1000", "Book not found.", HttpStatus.NOT_FOUND),
    AUTHOR_NOT_FOUND("E1001", "The selected author could not be found.", HttpStatus.NOT_FOUND),
    PUBLISHER_NOT_FOUND("E1003", "The selected publisher could not be found.", HttpStatus.NOT_FOUND),

    // --- Language domain (E2000 - E2099) ---
    LANGUAGE_NOT_FOUND("E2000", "The selected language could not be found.", HttpStatus.NOT_FOUND),
    LANGUAGE_IN_USE("E2001","The selected language is currently in use.",HttpStatus.CONFLICT),
    LANGUAGE_NAME_DUPLICATE("E0002", "A language with this name already exists.", HttpStatus.CONFLICT),

    // --- Genre domain (E2100 - E2199) ---
    GENRE_NOT_FOUND("E2100", "One or more selected genres could not be found.", HttpStatus.NOT_FOUND),
    GENRE_IN_USE("E2101","The selected genre is currently in use.",HttpStatus.CONFLICT),
    GENRE_NAME_DUPLICATE("E102", "A genre with this name already exists.", HttpStatus.CONFLICT),
    ;

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
