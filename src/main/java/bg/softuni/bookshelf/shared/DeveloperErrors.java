package bg.softuni.bookshelf.shared;

/**
 * Standardized internal error messages for developer logging and defense-in-depth assertions.
 */
public final class DeveloperErrors {
    public static final String DTO_NULL = "Incoming DTO must not be null.";
    public static final String ENTITY_ID_NULL = "Entity ID must not be null.";
    public static final String PAGEABLE_NULL = "Pageable parameter must not be null.";
    public static final String NAME_NULL = "Name must not be null.";

    private DeveloperErrors() {
        // Restrict instantiation
    }
}
