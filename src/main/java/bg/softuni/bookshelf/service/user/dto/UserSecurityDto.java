package bg.softuni.bookshelf.service.user.dto;

import java.util.UUID;

/**
 * Immutable security projection record.
 * Decouples web-tier security context updates from raw JPA entity boundaries.
 */
public record UserSecurityDto(
    UUID id,
    String username,
    boolean passwordChangeRequired
) {}
