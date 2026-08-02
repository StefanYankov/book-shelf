package bg.softuni.bookshelf.service.challenge;

import bg.softuni.bookshelf.service.challenge.dto.LogProgressDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeCreateDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeViewDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadingChallengeProxyService Unit Tests")
class ReadingChallengeProxyServiceTest {

    private final UUID challengeId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Mock
    private ReadingChallengeClient readingChallengeClient;

    @InjectMocks
    private ReadingChallengeProxyService proxyService;

    private ReadingChallengeViewDto view() {
        return new ReadingChallengeViewDto(challengeId, userId, 2026, 30, 0, false);
    }

    @Test
    @DisplayName("createChallenge delegates to the Feign client and returns its result")
    void shouldDelegateCreate() {
        // Arrange
        ReadingChallengeCreateDto dto = new ReadingChallengeCreateDto(2026, 30);
        ReadingChallengeViewDto expected = view();
        given(readingChallengeClient.createChallenge(dto)).willReturn(expected);

        // Act
        ReadingChallengeViewDto result = proxyService.createChallenge(dto);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(readingChallengeClient).createChallenge(dto);
    }

    @Test
    @DisplayName("logProgress delegates to the Feign client with the challenge id and body")
    void shouldDelegateLogProgress() {
        // Arrange
        LogProgressDto dto = new LogProgressDto(3);
        ReadingChallengeViewDto expected = view();
        given(readingChallengeClient.logProgress(eq(challengeId), any(LogProgressDto.class))).willReturn(expected);

        // Act
        ReadingChallengeViewDto result = proxyService.logProgress(challengeId, dto);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(readingChallengeClient).logProgress(challengeId, dto);
    }

    @Test
    @DisplayName("getChallenge delegates to the Feign client with the year")
    void shouldDelegateGetChallenge() {
        // Arrange
        ReadingChallengeViewDto expected = view();
        given(readingChallengeClient.getChallenge(2026)).willReturn(expected);

        // Act
        ReadingChallengeViewDto result = proxyService.getChallenge(2026);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(readingChallengeClient).getChallenge(2026);
    }
}