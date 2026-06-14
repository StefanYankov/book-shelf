package bg.softuni.bookshelf.shared;

/**
 * Global application constants to eliminate magic numbers and strings.
 * Used across Entities for database schema generation and DTOs for validation.
 */
public final class ValidationConstants {

    // User-related constants
    public static final class User {
        public static final int MAX_USERNAME_LENGTH = 50;
        public static final int MAX_EMAIL_LENGTH = 100;
        public static final int MAX_NAME_LENGTH = 50;
    }

    // Book-related constants
    public static final class Book {
        public static final int MAX_TITLE_LENGTH = 26000;
        public static final int MAX_SUMMARY_LENGTH = 1000;
    }

    // Author-related constants
    public static final class Author {
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MAX_SUMMARY_LENGTH = 1000;
    }

    // Publisher-related constants
    public static final class Publisher {
        public static final int MAX_NAME_LENGTH = 100;
    }

    // Genre-related constants
    public static final class Genre {
        public static final int MAX_NAME_LENGTH = 50;
        public static final int MAX_DESCRIPTION_LENGTH = 1000;
    }

    // Review-related constants
    public static final class Review {
        public static final int MAX_TITLE_LENGTH = 100;
    }

    // Review-related constants
    public static final class Language {
        public static final int MAX_NAME_LENGTH = 100;
    }

    private ValidationConstants() {
        // Restrict instantiation
    }
}
