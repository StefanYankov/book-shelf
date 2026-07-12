package bg.softuni.bookshelf.service.auth;

import bg.softuni.bookshelf.service.user.dto.UserSecurityDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

/**
 * Service for handling JSON Web Tokens (JWTs).
 * This includes generating new tokens, validating existing ones, and extracting claims.
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates a JWT for a user details object, including custom claims for user ID and role.
     *
     * @param userDetails The user details from Spring Security.
     * @return The generated JWT string.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        if (userDetails instanceof CustomUserDetails customUser) {
            extraClaims.put("userId", customUser.getId().toString());
            extraClaims.put("pwd_chg_req", customUser.isPasswordChangeRequired());
        }

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
        extraClaims.put("role", role);

        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Maps security claim payload and generates a token directly from a detached UserSecurityDto projection.
     *
     * @param user        The detached user security state.
     * @param authorities The authorities to grant the session context.
     * @return The signed JWT string.
     */
    public String generateTokenForUser(UserSecurityDto user, Collection<? extends GrantedAuthority> authorities) {
        // TODO: Refactor using an explicit stateless factory method to avoid magic empty string values.
        CustomUserDetails userDetails = new CustomUserDetails(
                user.id(),
                user.username(),
                "",
                true,
                user.passwordChangeRequired(),
                authorities.stream().map(auth -> (GrantedAuthority) auth).toList()
        );
        return generateToken(userDetails);
    }

    /**
     * Extracts the username from a JWT.
     *
     * @param token The JWT.
     * @return The username.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT.
     *
     * @param token The JWT.
     * @param claimsResolver A function to extract the claim.
     * @param <T> The type of the claim.
     * @return The claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Reconstructs a CustomUserDetails object directly from the claims of a verified JWT.
     * This avoids a database call on every request for a stateless authentication model.
     *
     * @param token The verified JWT.
     * @return An in-memory CustomUserDetails principal.
     */
    public CustomUserDetails extractUserDetails(String token) {
        Claims claims = extractAllClaims(token);
        String userId = claims.get("userId", String.class);
        String role = claims.get("role", String.class);
        boolean passwordChangeRequired = claims.get("pwd_chg_req", Boolean.class);

        return new CustomUserDetails(
                UUID.fromString(userId),
                claims.getSubject(),
                "", // Password is not needed for in-memory principal
                true,
                passwordChangeRequired,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    /**
     * Checks if a JWT is valid.
     *
     * @param token The JWT.
     * @param userDetails The user details to validate against.
     * @return True if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Evaluates if the cryptographic token expiration timestamp precedes the current system date.
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracts the expiration date claim from the token payload.
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Parses and cryptographically validates the JWT payload signature using the configured signing key.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Decodes the base64-encoded secret key and constructs an HMAC signing key.
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Builds a signed JWT carrying standard claims, expiration limits, and private authority payloads.
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }
}
