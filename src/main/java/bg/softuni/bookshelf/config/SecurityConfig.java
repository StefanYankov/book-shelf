package bg.softuni.bookshelf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Central configuration for Spring Security.
 * Defines security filters, authentication providers, and authorization rules.
 */
@Configuration
@Slf4j
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    /**
     * Allowed CORS origins, bound from configuration (app.cors.allowed-origins).
     * Production value comes from an env var; the dev profile injects localhost.
     * A comma-separated env string OR a YAML list both bind to List<String>.
     */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    /**
     * Public, unauthenticated system endpoints (API docs + monitoring).
     * Extracted to a constant for readability and reuse.
     */
    private static final String[] PUBLIC_MATCHERS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**"
    };

    /**
     * Configures the security filter chain that processes HTTP requests.
     * This is the entry point for all security-related decisions.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Actuator & Swagger (System Public)
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()

                        // 2. Auth Domain (Public)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 3. Domain Read-Only Access (Public GETs)
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()

                        // 4. Role-Restricted Zones (authorization gated on ROLE)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 5. Identity-Restricted Zones (any authenticated principal acting on
                        //    their own account — role-agnostic, so USER/ADMIN/future types all pass)
                        .requestMatchers("/api/users/me/**").authenticated()

                        // 6. Catch-All Default (Secure by Default)
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new PasswordChangeFilter(objectMapper), JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS for the application.
     * Allowed origins are now injected from configuration instead of hardcoded,
     * so deploying to a new host is a config change, not a code change.
     *
     * @return A CorsConfigurationSource.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Externalized: value differs per environment (dev = localhost, prod = real domain).
        configuration.setAllowedOrigins(allowedOrigins); // For wildcard subdomains (https://*.example.com), use setAllowedOriginPatterns(...) i
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // cache CORS pre-flight (OPTIONS) for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 401 handler for unauthenticated requests.
     * Emits RFC 7807 ProblemDetail JSON to stay consistent with the rest of the app
     * (JwtAuthenticationFilter, PasswordChangeFilter, GlobalExceptionHandler).
     */
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            log.debug("Filter-chain 401: unauthenticated request to {} {}", request.getMethod(), request.getRequestURI());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required to access this resource."
            );
            problem.setTitle("Unauthorized");
            problem.setType(URI.create("urn:bookshelf:unauthorized"));
            writeProblem(response, HttpStatus.UNAUTHORIZED, problem);
        };
    }

    /**
     * 403 handler for authenticated-but-forbidden requests hitting URL-based rules.
     * Mirrors the AccessDeniedException handling in GlobalExceptionHandler so that
     * filter-chain 403s and method-level (@PreAuthorize) 403s look identical to clients.
     */
    private AccessDeniedHandler forbiddenHandler() {
        return (request, response, ex) -> {
            log.warn("Filter-chain 403: {} {} denied for principal", request.getMethod(), request.getRequestURI());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                    "You do not have the required permissions to access this resource.");
            problem.setTitle("Forbidden");
            problem.setType(URI.create("urn:bookshelf:access-denied"));
            writeProblem(response, HttpStatus.FORBIDDEN, problem);
        };
    }

    /**
     * Shared writer to serialize a ProblemDetail using the app's ObjectMapper.
     */
    private void writeProblem(jakarta.servlet.http.HttpServletResponse response,
                              HttpStatus status,
                              ProblemDetail problem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}