package bg.softuni.bookshelf.config;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import bg.softuni.bookshelf.service.auth.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom Spring Security filter that intercepts every HTTP request to validate the JWT.
 * This implementation is stateless and reconstructs the user principal from the token claims,
 * avoiding a database call on every request.
 * <p>
 * The filter is permissive about bad tokens: a missing, malformed, or expired token results
 * in an unauthenticated request that is allowed to continue down the chain. Public endpoints
 * then proceed anonymously; protected endpoints are rejected by the configured
 * AuthenticationEntryPoint (which returns a consistent RFC 7807 401). The filter's job is to
 * populate the SecurityContext when a valid token is present — never to reject a request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            final String username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Completely database-free state reconstruction from cryptographically verified claims
                CustomUserDetails userDetails = jwtService.extractUserDetails(jwt);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException e) {
            // Invalid/expired token: do NOT reject here. Leave the context unauthenticated and
            // let the chain continue. Public endpoints proceed anonymously; protected ones are
            // rejected downstream by the AuthenticationEntryPoint (consistent 401 ProblemDetail).
            log.warn("JWT authentication skipped: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}