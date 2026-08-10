package bg.softuni.bookshelf.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit coverage for {@link OpenApiConfig}.
 */
class OpenApiConfigTest {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    private static BuildProperties buildPropertiesWithVersion(String version) {
        Properties source = new Properties();
        source.setProperty("version", version);
        return new BuildProperties(source);
    }

    @Test
    @DisplayName("Registers a bearerAuth HTTP scheme with JWT bearer format")
    void shouldRegisterBearerAuthScheme() {
        // Arrange
        OpenApiConfig config = new OpenApiConfig(Optional.of(buildPropertiesWithVersion("9.9.9")));

        // Act
        OpenAPI openAPI = config.bookShelfOpenAPI();

        // Assert
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey(BEARER_SCHEME_NAME);

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get(BEARER_SCHEME_NAME);
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    @DisplayName("Version tracks the injected build properties")
    void shouldUseVersionFromBuildProperties() {
        // Arrange
        OpenApiConfig config = new OpenApiConfig(Optional.of(buildPropertiesWithVersion("9.9.9")));

        // Act
        OpenAPI openAPI = config.bookShelfOpenAPI();

        // Assert
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Book Shelf API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("9.9.9");
    }

    @Test
    @DisplayName("Falls back to 'unknown' when build info is absent")
    void shouldFallBackWhenBuildInfoMissing() {
        // Arrange
        OpenApiConfig config = new OpenApiConfig(Optional.empty());

        // Act
        OpenAPI openAPI = config.bookShelfOpenAPI();

        // Assert
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("Applies bearerAuth as a global security requirement")
    void shouldApplyGlobalSecurityRequirement() {
        // Arrange
        OpenApiConfig config = new OpenApiConfig(Optional.of(buildPropertiesWithVersion("9.9.9")));

        // Act
        OpenAPI openAPI = config.bookShelfOpenAPI();

        // Assert
        assertThat(openAPI.getSecurity())
                .isNotNull()
                .anySatisfy(req -> assertThat(req).containsKey("bearerAuth"));
    }
}
