package bg.softuni.bookshelf.shared;

/**
 * Global application constants to eliminate magic numbers and strings.
 * Used across Entities for database schema generation and DTOs for validation.
 */
public final class ValidationConstants {

    // User-related constants
    public static final class User {
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 50;
        public static final int MAX_EMAIL_LENGTH = 100;
        public static final int MIN_FIRST_NAME_LENGTH = 2;
        public static final int MAX_FIRST_NAME_LENGTH = 50;
        public static final int MIN_LAST_NAME_LENGTH = 2;
        public static final int MAX_LAST_NAME_LENGTH = 50;
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 100;
    }

    // Bookshelf-related constants
    public static final class Bookshelf {
        public static final int MIN_NAME_LENGTH = 3;
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MAX_DESCRIPTION_LENGTH = 500;
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
        public static final int MAX_COMMENT_LENGTH = 2000;
        public static final int TARGET_TYPE_MAX_LENGTH = 50;
        public static final int MIN_RATING = 1;
        public static final int MAX_RATING = 5;
    }

    // Language-related constants
    public static final class Language {
        public static final int MAX_NAME_LENGTH = 100;
    }

    // Reading challenge-related constants
    public static final class Challenge {

        public static final int MIN_YEAR = 2000;
        public static final int MAX_YEAR = 2100;
        public static final int MIN_GOAL = 1;
        public static final int MAX_GOAL = 1000;
        public static final int MIN_PROGRESS = 0;
        public static final int MAX_PROGRESS = 1000;
    }
    private ValidationConstants() {
        // Restrict instantiation
    }
}