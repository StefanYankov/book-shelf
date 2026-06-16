package bg.softuni.bookshelf.service.auth.dto;

/**
 * Defines the API contract for a successful authentication response.
 *
 * @param token The JWT to be used for subsequent authenticated requests.
 */
public record AuthenticationResponse(String token) {
}
