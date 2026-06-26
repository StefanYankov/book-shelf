package bg.softuni.bookshelf.config;

import bg.softuni.bookshelf.service.auth.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

public class PasswordChangeFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    public PasswordChangeFilter(ObjectMapper objectMapper, JwtService jwtService) {
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        boolean passwordChangeRequired = jwtService.extractClaim(jwt, claims -> claims.get("pwd_chg_req", Boolean.class));

        if (passwordChangeRequired && !request.getRequestURI().equals("/api/users/me/password")) {
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
            problemDetail.setTitle("Password Change Required");
            problemDetail.setDetail("You must change your password before you can perform other actions.");
            problemDetail.setInstance(URI.create(request.getRequestURI()));
            problemDetail.setType(URI.create("urn:bookshelf:password-change-required"));

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
