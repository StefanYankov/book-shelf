package bg.softuni.bookshelf.config;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

/**
 * Filter that intercepts incoming HTTP requests to enforce mandatory password rotation.
 * Bypasses public authentication paths to prevent lockouts caused by stale client tokens.
 */
@RequiredArgsConstructor
public class PasswordChangeFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        boolean isPublicAuthPath = path.startsWith("/api/auth/");
        if (isPublicAuthPath) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            if (userDetails.isPasswordChangeRequired()) {
                String method = request.getMethod();
                boolean isPasswordChangeRequest = "PUT".equalsIgnoreCase(method) && path.endsWith("/api/users/me/password");

                if (!isPasswordChangeRequest) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                            HttpStatus.FORBIDDEN,
                            "A password change is required before accessing this resource."
                    );
                    problemDetail.setType(URI.create("urn:bookshelf:password-change-required"));
                    problemDetail.setTitle("Password Change Required");

                    objectMapper.writeValue(response.getWriter(), problemDetail);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
