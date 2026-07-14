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
    ACCESS_DENIED("E0003", "You do not have permission to perform this action.", HttpStatus.FORBIDDEN),

    // --- Authentication (E1000 - E1099) ---
    USERNAME_ALREADY_EXISTS("E1000", "A user with this username already exists.", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("E1001", "A user with this email address already exists.", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("E1002", "Invalid username or password.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("E1003", "The provided token is invalid or has expired.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("E1004", "The provided token has expired. Please request a new one.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("E1005", "User not found.", HttpStatus.NOT_FOUND),
    SELF_LOCK_PREVENTION("E1006", "Administrators are not allowed to lock their own accounts.", HttpStatus.FORBIDDEN),

    // --- Book domain (E1100 - E1199) ----
    BOOK_NOT_FOUND("E1100", "Book not found.", HttpStatus.NOT_FOUND),

    // --- Bookshelf domain (E1200 - E1299) ---
    BOOKSHELF_NOT_FOUND("E1200", "Bookshelf not found.", HttpStatus.NOT_FOUND),
    BOOK_ALREADY_IN_SHELF("E1201", "This book is already on this bookshelf.", HttpStatus.CONFLICT),
    BOOK_NOT_IN_SHELF("E1202", "This book is not on this bookshelf.", HttpStatus.NOT_FOUND),

    // --- Language domain (E2000 - E2099) ---
    LANGUAGE_NOT_FOUND("E2000", "The selected language could not be found.", HttpStatus.NOT_FOUND),
    LANGUAGE_IN_USE("E2001","The selected language is currently in use.",HttpStatus.CONFLICT),
    LANGUAGE_NAME_DUPLICATE("E2002", "A language with this name already exists.", HttpStatus.CONFLICT),

    // --- Genre domain (E2100 - E2199) ---
    GENRE_NOT_FOUND("E2100", "One or more selected genres could not be found.", HttpStatus.NOT_FOUND),
    GENRE_IN_USE("E2101","The selected genre is currently in use.",HttpStatus.CONFLICT),
    GENRE_NAME_DUPLICATE("E2102", "A genre with this name already exists.", HttpStatus.CONFLICT),

    // --- Publisher domain (E2200 - E2299) ---
    PUBLISHER_NOT_FOUND("E2200", "The selected publisher could not be found.", HttpStatus.NOT_FOUND),
    PUBLISHER_IN_USE("E2201","The selected publisher is currently in use.",HttpStatus.CONFLICT),
    PUBLISHER_NAME_DUPLICATE("E2202", "A publisher with this name already exists.", HttpStatus.CONFLICT),

    // --- Author domain (E2300 - E2399) ---
    AUTHOR_NOT_FOUND("E2300", "The selected author could not be found.", HttpStatus.NOT_FOUND),
    AUTHOR_IN_USE("E2301","The selected author is currently in use.",HttpStatus.CONFLICT),
    AUTHOR_NAME_DUPLICATE("E2302", "An author with this name already exists.", HttpStatus.CONFLICT),

    // --- Review domain (E2400 - E2499) ---
    REVIEW_NOT_FOUND("E2400", "Review not found.", HttpStatus.NOT_FOUND),
    DUPLICATE_REVIEW("E2401", "You have already reviewed this item.", HttpStatus.CONFLICT),
    UNAUTHORIZED_REVIEW_MODIFICATION("E2402", "You cannot modify a review you did not author.", HttpStatus.FORBIDDEN)

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
