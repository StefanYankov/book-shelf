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
    AUTHOR_NOT_FOUND("E1001", "The selected author could not be found.", HttpStatus.BAD_REQUEST),
    LANGUAGE_NOT_FOUND("E1002", "The selected language could not be found.", HttpStatus.BAD_REQUEST),
    PUBLISHER_NOT_FOUND("E1003", "The selected publisher could not be found.", HttpStatus.BAD_REQUEST),
    GENRE_NOT_FOUND("E1004", "One or more selected genres could not be found.", HttpStatus.BAD_REQUEST)

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
