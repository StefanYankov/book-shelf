package bg.softuni.bookshelf.integration;

import bg.softuni.bookshelf.service.challenge.ReadingChallengeClient;
import bg.softuni.bookshelf.service.challenge.dto.LogProgressDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeCreateDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeViewDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "reading-challenge.service.url=http://localhost:${wiremock.server.port}")
@DisplayName("ReadingChallengeClient Feign Integration Tests")
class ReadingChallengeClientTest {

    private static final String CHALLENGE_JSON = """
            {
              "id": "%s",
              "userId": "%s",
              "year": 2026,
              "goal": 30,
              "booksRead": 0,
              "completed": false
            }
            """;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ReadingChallengeClient readingChallengeClient;

    @BeforeEach
    void setUp() {
        // Simulate an incoming request carrying the caller's JWT, so the Feign
        // interceptor has an Authorization header to forward.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-jwt-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        WireMock.reset();
    }

    @Test
    @DisplayName("createChallenge sends the body, forwards the JWT, and maps the response")
    void shouldCreateChallengeAndForwardToken() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubFor(post(urlEqualTo("/api/challenges"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(CHALLENGE_JSON.formatted(id, userId))));

        // Act
        ReadingChallengeViewDto result = readingChallengeClient.createChallenge(new ReadingChallengeCreateDto(2026, 30));

        // Assert
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.goal()).isEqualTo(30);

        verify(postRequestedFor(urlEqualTo("/api/challenges"))
                .withHeader("Authorization", equalTo("Bearer test-jwt-token"))
                .withRequestBody(matchingJsonPath("$.year", equalTo("2026")))
                .withRequestBody(matchingJsonPath("$.goal", equalTo("30"))));
    }

    @Test
    @DisplayName("logProgress calls the correct path, forwards the JWT, and sends the body")
    void shouldLogProgress() {
        // Arrange
        UUID challengeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubFor(put(urlEqualTo("/api/challenges/" + challengeId + "/progress"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(CHALLENGE_JSON.formatted(challengeId, userId))));

        // Act
        readingChallengeClient.logProgress(challengeId, new LogProgressDto(3));

        // Assert
        verify(putRequestedFor(urlEqualTo("/api/challenges/" + challengeId + "/progress"))
                .withHeader("Authorization", equalTo("Bearer test-jwt-token"))
                .withRequestBody(matchingJsonPath("$.booksRead", equalTo("3"))));
    }

    @Test
    @DisplayName("getChallenge sends the year query parameter and forwards the JWT")
    void shouldGetChallenge() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/api/challenges"))
                .withQueryParam("year", equalTo("2026"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(CHALLENGE_JSON.formatted(id, userId))));

        // Act
        ReadingChallengeViewDto result = readingChallengeClient.getChallenge(2026);

        // Assert
        assertThat(result.year()).isEqualTo(2026);
        verify(getRequestedFor(urlPathEqualTo("/api/challenges"))
                .withQueryParam("year", equalTo("2026"))
                .withHeader("Authorization", equalTo("Bearer test-jwt-token")));
    }

    @Test
    @DisplayName("Translates a downstream 409 into DUPLICATE_CHALLENGE via the error decoder")
    void shouldTranslateConflict() {
        // Arrange
        stubFor(post(urlEqualTo("/api/challenges"))
                .willReturn(aResponse().withStatus(409)));

        // Act & Assert
        assertThatThrownBy(() -> readingChallengeClient.createChallenge(new ReadingChallengeCreateDto(2026, 30)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_CHALLENGE);
    }

    @Test
    @DisplayName("Translates a downstream 404 into CHALLENGE_NOT_FOUND via the error decoder")
    void shouldTranslateNotFound() {
        // Arrange
        UUID challengeId = UUID.randomUUID();
        stubFor(put(urlEqualTo("/api/challenges/" + challengeId + "/progress"))
                .willReturn(aResponse().withStatus(404)));

        // Act & Assert
        assertThatThrownBy(() -> readingChallengeClient.logProgress(challengeId, new LogProgressDto(1)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CHALLENGE_NOT_FOUND);
    }
}