package bg.softuni.bookshelf.shared.exception;

import lombok.Getter;

/**
 * Base unchecked exception for all intentional business logic violations.
 * Triggers a standardized JSON error response via @ControllerAdvice.
 * <p>
 * This exception uses a centralized {@link ErrorCode} to provide consistent
 * error messaging and HTTP status codes across the entire application.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Constructs a new BusinessException using the default message provided
     * by the {@link ErrorCode}.
     *
     * @param errorCode the business error code defining the status and default description.
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new BusinessException using the default message from the
     * {@link ErrorCode} appended with specific dynamic details.
     * <p>
     * Use this constructor when you need to provide context-specific information,
     * such as an ID that was not found or a name that already exists.
     *
     * @param errorCode      the business error code defining the status and base description.
     * @param dynamicDetail  specific details to append to the error message (e.g., "ID: 123").
     */
    public BusinessException(ErrorCode errorCode, String dynamicDetail) {
        super(String.format("%s (%s)", errorCode.getDefaultMessage(), dynamicDetail));
        this.errorCode = errorCode;
    }
}